package com.example.prayertime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertime.alarm.PrayerAlarmPlanner
import com.example.prayertime.data.PrayerTimeCalculator
import com.example.prayertime.data.SettingsRepository
import com.example.prayertime.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val calculator = PrayerTimeCalculator()
    private val locationProvider = LocationProvider(application)
    private val planner = PrayerAlarmPlanner(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.settingsFlow, repository.alarmsFlow) { settings, alarms ->
                settings to alarms
            }.collect { (settings, alarms) ->
                refreshDerivedState(settings, alarms, null)
                planner.scheduleAll(settings, alarms)
            }
        }
    }

    fun refreshPrayerTimes() {
        viewModelScope.launch {
            val settings = repository.settingsFlow.first()
            val alarms = repository.alarmsFlow.first()
            refreshDerivedState(settings, alarms, "تم تحديث المواقيت")
        }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = repository.settingsFlow.first()
            repository.saveSettings(transform(current))
        }
    }

    fun chooseTone(uriString: String) {
        updateSettings { it.copy(selectedToneUri = uriString) }
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val current = repository.settingsFlow.first()
            repository.saveSettings(
                current.copy(
                    countryMode = CountryMode.AUTO,
                    locationPreset = null,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun fetchAutoLocation() {
        viewModelScope.launch {
            val latLng = locationProvider.getCurrentLatLng()
            if (latLng != null) {
                val current = repository.settingsFlow.first()
                repository.saveSettings(
                    current.copy(
                        countryMode = CountryMode.AUTO,
                        locationPreset = null,
                        latitude = latLng.first,
                        longitude = latLng.second
                    )
                )
                _uiState.value = _uiState.value.copy(statusMessage = "تم تحديث الموقع عبر GPS")
            } else {
                _uiState.value = _uiState.value.copy(statusMessage = "تعذر جلب الموقع")
            }
        }
    }

    fun addAlarm(alarm: PrayerAlarmItem) {
        viewModelScope.launch {
            repository.upsertAlarm(alarm)
            _uiState.value = _uiState.value.copy(statusMessage = "تمت إضافة المنبه")
        }
    }

    fun updateAlarm(alarm: PrayerAlarmItem) {
        viewModelScope.launch {
            repository.upsertAlarm(alarm)
            _uiState.value = _uiState.value.copy(statusMessage = "تم تحديث المنبه")
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            AlarmRemovalHelper.cancelImmediately(getApplication(), alarmId)
            repository.deleteAlarm(alarmId)
            _uiState.value = _uiState.value.copy(statusMessage = "تم حذف المنبه")
        }
    }

    fun setAlarmEnabled(alarm: PrayerAlarmItem, enabled: Boolean) {
        updateAlarm(alarm.copy(enabled = enabled))
    }

    private suspend fun refreshDerivedState(settings: AppSettings, alarms: List<PrayerAlarmItem>, message: String?) {
        val zoneId = ZoneId.systemDefault()
        val coords = resolveCoordinatesForCurrentMode(settings)
        val previews = buildAlarmPreviews(settings, alarms, zoneId)

        if (coords == null) {
            _uiState.value = _uiState.value.copy(
                alarmPreviews = previews,
                schedule = PrayerSchedule(nextAlarm = nextAlarmLabel(settings, alarms, zoneId)),
                statusMessage = message ?: "لا يوجد موقع محفوظ بعد",
                settings = settings,
                alarms = alarms,
                loading = false
            )
            return
        }

        val today = LocalDate.now(zoneId)
        val times = calculator.calculate(
            latitude = coords.first,
            longitude = coords.second,
            countryMode = settings.countryMode,
            date = today,
            zoneId = zoneId
        )

        val schedule = PrayerSchedule(
            fajr = calculator.formatInstant(times.fajr, zoneId),
            dhuhr = calculator.formatInstant(times.dhuhr, zoneId),
            asr = calculator.formatInstant(times.asr, zoneId),
            maghrib = calculator.formatInstant(times.maghrib, zoneId),
            isha = calculator.formatInstant(times.isha, zoneId),
            nextAlarm = nextAlarmLabel(settings, alarms, zoneId)
        )

        _uiState.value = _uiState.value.copy(
            schedule = schedule,
            alarmPreviews = previews,
            settings = settings,
            alarms = alarms,
            loading = false,
            statusMessage = message ?: _uiState.value.statusMessage
        )
    }

    private fun buildAlarmPreviews(
        settings: AppSettings,
        alarms: List<PrayerAlarmItem>,
        zoneId: ZoneId
    ): Map<String, AlarmPreviewInfo> {
        val now = Instant.now()
        return alarms.associate { alarm ->
            val preview = planner.previewNextTrigger(settings, alarm, now, zoneId)
            val info = when {
                !alarm.enabled -> AlarmPreviewInfo("متوقف", "--")
                preview == null -> AlarmPreviewInfo("غير متاح", "--")
                else -> AlarmPreviewInfo(
                    exactTimeLabel = exactTriggerLabel(preview, zoneId),
                    countdownLabel = countdownLabel(preview, now)
                )
            }
            alarm.id to info
        }
    }

    private fun nextAlarmLabel(
        settings: AppSettings,
        alarms: List<PrayerAlarmItem>,
        zoneId: ZoneId
    ): String {
        val now = Instant.now()
        val next = alarms.asSequence()
            .filter { it.enabled }
            .mapNotNull { alarm -> planner.previewNextTrigger(settings, alarm, now, zoneId) }
            .minOrNull()
            ?: return "لا يوجد منبه"

        return calculator.formatInstant(next, zoneId)
    }

    private fun resolveCoordinatesForCurrentMode(settings: AppSettings): Pair<Double, Double>? {
        parseLocationPreset(settings.locationPreset)?.let { preset ->
            return preset.coordinates()
        }

        settings.latitude?.let { lat ->
            settings.longitude?.let { lon ->
                return lat to lon
            }
        }

        return when (settings.countryMode) {
            CountryMode.SYRIA -> LocationPreset.SYRIA_DAMASCUS.coordinates()
            CountryMode.JORDAN -> LocationPreset.JORDAN_AMMAN.coordinates()
            CountryMode.AUTO -> null
        }
    }
}

private object AlarmRemovalHelper {
    fun cancelImmediately(application: Application, alarmId: String) {
        com.example.prayertime.alarm.AlarmScheduler(application).cancelAlarm(alarmId)
    }
}
