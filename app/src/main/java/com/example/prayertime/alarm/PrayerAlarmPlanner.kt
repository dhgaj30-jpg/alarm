package com.example.prayertime.alarm

import android.content.Context
import com.example.prayertime.AlarmType
import com.example.prayertime.AppSettings
import com.example.prayertime.CountryMode
import com.example.prayertime.LocationPreset
import com.example.prayertime.PrayerAlarmItem
import com.example.prayertime.PrayerName
import com.example.prayertime.data.PrayerTimeCalculator
import com.example.prayertime.data.PrayerTimesData
import com.example.prayertime.parseLocationPreset
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PrayerAlarmPlanner(private val context: Context) {

    fun scheduleAll(settings: AppSettings, alarms: List<PrayerAlarmItem>) {
        alarms.forEach { alarm ->
            val trigger = previewNextTrigger(settings, alarm) ?: run {
                AlarmScheduler(context).cancelAlarm(alarm.id)
                return@forEach
            }

            if (alarm.enabled) {
                AlarmScheduler(context).scheduleAlarm(alarm, trigger.toEpochMilli(), settings)
            } else {
                AlarmScheduler(context).cancelAlarm(alarm.id)
            }
        }
    }

    fun previewNextTrigger(
        settings: AppSettings,
        alarm: PrayerAlarmItem,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Instant? {
        if (!alarm.enabled) return null

        return when (alarm.type) {
            AlarmType.MANUAL -> nextManualTrigger(alarm, now, zoneId)
            AlarmType.PRAYER -> {
                val coordinates = resolveCoordinates(settings) ?: return null
                nextPrayerTrigger(alarm, coordinates, settings, now, zoneId)
            }
        }
    }

    private fun nextManualTrigger(alarm: PrayerAlarmItem, now: Instant, zoneId: ZoneId): Instant? {
        val today = LocalDate.now(zoneId)
        val target = today.atTime(
            LocalTime.of(alarm.manualHour.coerceIn(0, 23), alarm.manualMinute.coerceIn(0, 59))
        ).atZone(zoneId).toInstant()
        return when {
            target.isAfter(now) -> target
            alarm.repeatDaily -> target.plus(Duration.ofDays(1))
            else -> null
        }
    }

    private fun nextPrayerTrigger(
        alarm: PrayerAlarmItem,
        coordinates: Pair<Double, Double>,
        settings: AppSettings,
        now: Instant,
        zoneId: ZoneId
    ): Instant? {
        val calc = PrayerTimeCalculator()
        val today = LocalDate.now(zoneId)
        val todayTimes = calc.calculate(
            latitude = coordinates.first,
            longitude = coordinates.second,
            countryMode = settings.countryMode,
            date = today,
            zoneId = zoneId
        )

        val todaySelected = getSelectedPrayerInstant(alarm.prayerName, todayTimes)
        val adjustedToday = applyOffset(todaySelected, alarm.offsetMode, alarm.offsetMinutes)
        if (adjustedToday.isAfter(now)) return adjustedToday
        if (!alarm.repeatDaily) return null

        val tomorrow = today.plusDays(1)
        val tomorrowTimes = calc.calculate(
            latitude = coordinates.first,
            longitude = coordinates.second,
            countryMode = settings.countryMode,
            date = tomorrow,
            zoneId = zoneId
        )
        val tomorrowSelected = getSelectedPrayerInstant(alarm.prayerName, tomorrowTimes)
        return applyOffset(tomorrowSelected, alarm.offsetMode, alarm.offsetMinutes)
    }

    private fun getSelectedPrayerInstant(prayerName: PrayerName, times: PrayerTimesData): Instant {
        return when (prayerName) {
            PrayerName.FAJR -> times.fajr
            PrayerName.DHUHR -> times.dhuhr
            PrayerName.ASR -> times.asr
            PrayerName.MAGHRIB -> times.maghrib
            PrayerName.ISHA -> times.isha
        }
    }

    private fun applyOffset(instant: Instant, mode: com.example.prayertime.OffsetMode, minutes: Int): Instant {
        val duration = Duration.ofMinutes(minutes.coerceIn(1, 60).toLong())
        return when (mode) {
            com.example.prayertime.OffsetMode.BEFORE -> instant.minus(duration)
            com.example.prayertime.OffsetMode.AFTER -> instant.plus(duration)
        }
    }

    private fun resolveCoordinates(settings: AppSettings): Pair<Double, Double>? {
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
