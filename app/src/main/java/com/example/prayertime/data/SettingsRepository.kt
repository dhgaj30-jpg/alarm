package com.example.prayertime.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prayertime.AlarmType
import com.example.prayertime.AppSettings
import com.example.prayertime.CountryMode
import com.example.prayertime.OffsetMode
import com.example.prayertime.PrayerAlarmItem
import com.example.prayertime.PrayerName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val countryMode = stringPreferencesKey("countryMode")
        val locationPreset = stringPreferencesKey("locationPreset")
        val selectedToneUri = stringPreferencesKey("selectedToneUri")
        val latitude = doublePreferencesKey("latitude")
        val longitude = doublePreferencesKey("longitude")
        val alarmsJson = stringPreferencesKey("alarmsJson")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            countryMode = CountryMode.valueOf(prefs[Keys.countryMode] ?: CountryMode.AUTO.name),
            locationPreset = prefs[Keys.locationPreset],
            selectedToneUri = prefs[Keys.selectedToneUri],
            latitude = prefs[Keys.latitude],
            longitude = prefs[Keys.longitude]
        )
    }

    val alarmsFlow: Flow<List<PrayerAlarmItem>> = context.dataStore.data.map { prefs ->
        decodeAlarms(prefs[Keys.alarmsJson])
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.countryMode] = settings.countryMode.name
            if (settings.locationPreset == null) {
                prefs.remove(Keys.locationPreset)
            } else {
                prefs[Keys.locationPreset] = settings.locationPreset
            }
            if (settings.selectedToneUri == null) {
                prefs.remove(Keys.selectedToneUri)
            } else {
                prefs[Keys.selectedToneUri] = settings.selectedToneUri
            }
            if (settings.latitude == null) {
                prefs.remove(Keys.latitude)
            } else {
                prefs[Keys.latitude] = settings.latitude
            }
            if (settings.longitude == null) {
                prefs.remove(Keys.longitude)
            } else {
                prefs[Keys.longitude] = settings.longitude
            }
        }
    }

    suspend fun updateLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.latitude] = latitude
            prefs[Keys.longitude] = longitude
        }
    }

    suspend fun saveAlarms(alarms: List<PrayerAlarmItem>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.alarmsJson] = encodeAlarms(alarms)
        }
    }

    suspend fun upsertAlarm(alarm: PrayerAlarmItem) {
        val current = alarmsFlow.first().toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index >= 0) {
            current[index] = alarm
        } else {
            current.add(alarm)
        }
        saveAlarms(current)
    }

    suspend fun deleteAlarm(alarmId: String) {
        val current = alarmsFlow.first().filterNot { it.id == alarmId }
        saveAlarms(current)
    }

    suspend fun findAlarmById(alarmId: String): PrayerAlarmItem? {
        return alarmsFlow.first().firstOrNull { it.id == alarmId }
    }

    private fun encodeAlarms(alarms: List<PrayerAlarmItem>): String {
        val array = JSONArray()
        alarms.forEach { alarm ->
            array.put(JSONObject().apply {
                put("id", alarm.id)
                put("title", alarm.title)
                put("enabled", alarm.enabled)
                put("type", alarm.type.name)
                put("manualHour", alarm.manualHour)
                put("manualMinute", alarm.manualMinute)
                put("prayerName", alarm.prayerName.name)
                put("offsetMode", alarm.offsetMode.name)
                put("offsetMinutes", alarm.offsetMinutes)
                put("repeatDaily", alarm.repeatDaily)
                put("snoozeMinutes", alarm.snoozeMinutes)
            })
        }
        return array.toString()
    }

    private fun decodeAlarms(raw: String?): List<PrayerAlarmItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    add(
                        PrayerAlarmItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", ""),
                            enabled = obj.optBoolean("enabled", true),
                            type = AlarmType.valueOf(obj.optString("type", AlarmType.PRAYER.name)),
                            manualHour = obj.optInt("manualHour", 5).coerceIn(0, 23),
                            manualMinute = obj.optInt("manualMinute", 0).coerceIn(0, 59),
                            prayerName = PrayerName.valueOf(obj.optString("prayerName", PrayerName.FAJR.name)),
                            offsetMode = OffsetMode.valueOf(obj.optString("offsetMode", OffsetMode.BEFORE.name)),
                            offsetMinutes = obj.optInt("offsetMinutes", 2).coerceIn(1, 60),
                            repeatDaily = obj.optBoolean("repeatDaily", true),
                            snoozeMinutes = obj.optInt("snoozeMinutes", 5).coerceIn(1, 60)
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
