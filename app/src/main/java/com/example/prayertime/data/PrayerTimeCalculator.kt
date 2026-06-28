package com.example.prayertime.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerAdjustments
import com.batoulapps.adhan.PrayerTimes
import com.example.prayertime.CountryMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PrayerTimesData(
    val fajr: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant
)

class PrayerTimeCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        countryMode: CountryMode,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): PrayerTimesData {
        val coordinates = Coordinates(latitude, longitude)

        val method = when (countryMode) {
            CountryMode.SYRIA -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            CountryMode.JORDAN -> CalculationMethod.EGYPTIAN
            CountryMode.AUTO -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }

        val params = method.parameters.copy(
            madhab = Madhab.HANAFI,
            prayerAdjustments = PrayerAdjustments(
                fajr = 0,
                dhuhr = 0,
                asr = 0,
                maghrib = 0,
                isha = 0
            )
        )

        val dc = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val prayerTimes = PrayerTimes(coordinates, dc, params)

        return PrayerTimesData(
            fajr = Instant.ofEpochMilli(prayerTimes.fajr.toEpochMilliseconds()),
            dhuhr = Instant.ofEpochMilli(prayerTimes.dhuhr.toEpochMilliseconds()),
            asr = Instant.ofEpochMilli(prayerTimes.asr.toEpochMilliseconds()),
            maghrib = Instant.ofEpochMilli(prayerTimes.maghrib.toEpochMilliseconds()),
            isha = Instant.ofEpochMilli(prayerTimes.isha.toEpochMilliseconds())
        )
    }

    fun formatInstant(instant: Instant, zoneId: ZoneId): String {
        val time = instant.atZone(zoneId).toLocalTime()
        val hour = time.hour
        val minute = time.minute
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val amPm = if (hour < 12) "ص" else "م"
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }
}
