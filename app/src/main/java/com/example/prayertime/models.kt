package com.example.prayertime

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class CountryMode { AUTO, SYRIA, JORDAN }
enum class PrayerName { FAJR, DHUHR, ASR, MAGHRIB, ISHA }
enum class OffsetMode { BEFORE, AFTER }
enum class AlarmType { MANUAL, PRAYER }

enum class LocationPreset(
    val countryMode: CountryMode,
    val labelText: String,
    val latitude: Double,
    val longitude: Double
) {
    JORDAN_AMMAN(CountryMode.JORDAN, "عمان", 31.9539, 35.9106),
    JORDAN_MAFRAQ(CountryMode.JORDAN, "المفرق", 32.3399, 36.2052),
    JORDAN_UMM_AL_JIMAL(CountryMode.JORDAN, "أم الجمال", 32.3410, 36.3860),
    SYRIA_DAMASCUS(CountryMode.SYRIA, "دمشق", 33.5138, 36.2765),
    SYRIA_RIF_DIMASHQ(CountryMode.SYRIA, "ريف دمشق", 33.5500, 36.5000),
    SYRIA_DARAA(CountryMode.SYRIA, "درعا", 32.6189, 36.1021),
    SYRIA_LAJAT(CountryMode.SYRIA, "منطقة اللجاة في درعا", 32.8000, 36.6500);

    fun coordinates(): Pair<Double, Double> = latitude to longitude
}

data class PrayerSchedule(
    val fajr: String = "--:--",
    val dhuhr: String = "--:--",
    val asr: String = "--:--",
    val maghrib: String = "--:--",
    val isha: String = "--:--",
    val nextAlarm: String = "غير محدد"
)

data class AppSettings(
    val countryMode: CountryMode = CountryMode.AUTO,
    val locationPreset: String? = null,
    val selectedToneUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class PrayerAlarmItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val enabled: Boolean = true,
    val type: AlarmType = AlarmType.PRAYER,
    val manualHour: Int = 5,
    val manualMinute: Int = 0,
    val prayerName: PrayerName = PrayerName.FAJR,
    val offsetMode: OffsetMode = OffsetMode.BEFORE,
    val offsetMinutes: Int = 2,
    val repeatDaily: Boolean = true,
    val snoozeMinutes: Int = 5
)

data class AlarmPreviewInfo(
    val exactTimeLabel: String = "غير محدد",
    val countdownLabel: String = "غير محدد"
)

data class PrayerUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val alarms: List<PrayerAlarmItem> = emptyList(),
    val alarmPreviews: Map<String, AlarmPreviewInfo> = emptyMap(),
    val schedule: PrayerSchedule = PrayerSchedule(),
    val statusMessage: String = ""
)

fun CountryMode.label(): String = when (this) {
    CountryMode.AUTO -> "تلقائي GPS"
    CountryMode.SYRIA -> "سوريا"
    CountryMode.JORDAN -> "الأردن"
}

fun PrayerName.label(): String = when (this) {
    PrayerName.FAJR -> "الفجر"
    PrayerName.DHUHR -> "الظهر"
    PrayerName.ASR -> "العصر"
    PrayerName.MAGHRIB -> "المغرب"
    PrayerName.ISHA -> "العشاء"
}

fun OffsetMode.label(): String = when (this) {
    OffsetMode.BEFORE -> "قبل"
    OffsetMode.AFTER -> "بعد"
}

fun AlarmType.label(): String = when (this) {
    AlarmType.MANUAL -> "منبه يدوي"
    AlarmType.PRAYER -> "مرتبط بصلاة"
}

fun LocationPreset.label(): String = labelText

fun locationPresetsFor(countryMode: CountryMode): List<LocationPreset> =
    LocationPreset.entries.filter { it.countryMode == countryMode }

fun defaultLocationPresetFor(countryMode: CountryMode): LocationPreset? = when (countryMode) {
    CountryMode.AUTO -> null
    CountryMode.JORDAN -> LocationPreset.JORDAN_AMMAN
    CountryMode.SYRIA -> LocationPreset.SYRIA_DAMASCUS
}

fun parseLocationPreset(name: String?): LocationPreset? {
    if (name.isNullOrBlank()) return null
    return runCatching { LocationPreset.valueOf(name) }.getOrNull()
}

fun PrayerAlarmItem.displayTitle(): String {
    val base = title.trim()
    if (base.isNotEmpty()) return base
    return when (type) {
        AlarmType.MANUAL -> String.format("منبه %02d:%02d", manualHour, manualMinute)
        AlarmType.PRAYER -> "منبه ${prayerName.label()}"
    }
}

fun PrayerAlarmItem.summary(): String = when (type) {
    AlarmType.MANUAL -> {
        val repeatText = if (repeatDaily) "يوميًا" else "مرة واحدة"
        "$repeatText عند ${String.format("%02d:%02d", manualHour, manualMinute)}"
    }
    AlarmType.PRAYER -> {
        val repeatText = if (repeatDaily) "يوميًا" else "مرة واحدة"
        "$repeatText ${offsetMode.label()} ${offsetMinutes} دقيقة من ${prayerName.label()}"
    }
}

fun exactTriggerLabel(instant: Instant, zoneId: ZoneId): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(zoneId)
    return formatter.format(instant)
}

fun countdownLabel(target: Instant, now: Instant = Instant.now()): String {
    if (target.isBefore(now)) return "الآن"
    val duration = Duration.between(now, target)
    val totalMinutes = duration.toMinutes()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours <= 0 && minutes <= 0 -> "خلال أقل من دقيقة"
        hours > 0 && minutes > 0 -> "بعد ${hours} س ${minutes} د"
        hours > 0 -> "بعد ${hours} س"
        else -> "بعد ${minutes} د"
    }
}
