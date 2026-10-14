package com.example.domain.util

import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.TimeFormatPreference
import com.example.domain.model.WindSpeedUnit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object UnitConverter {

    fun formatTemperature(
        celsius: Double,
        unit: TemperatureUnit,
        includeDegreeSign: Boolean = false
    ): String {
        val value = when (unit) {
            TemperatureUnit.CELSIUS -> celsius.roundToInt()
            TemperatureUnit.FAHRENHEIT -> ((celsius * 9.0 / 5.0) + 32.0).roundToInt()
        }
        return if (includeDegreeSign) "$value°" else "$value"
    }

    fun formatWindSpeed(
        kmh: Double,
        unit: WindSpeedUnit
    ): String {
        return when (unit) {
            WindSpeedUnit.KMH -> "${kmh.roundToInt()}km/h"
            WindSpeedUnit.MPH -> "${(kmh * 0.621371).roundToInt()}mph"
            WindSpeedUnit.MS -> "${String.format(Locale.US, "%.1f", kmh / 3.6)}m/s"
        }
    }

    fun formatPrecipitation(
        mm: Double,
        unit: PrecipitationUnit
    ): String {
        return when (unit) {
            PrecipitationUnit.MM -> "${mm.roundToInt()}mm"
            PrecipitationUnit.INCH -> "${String.format(Locale.US, "%.2f", mm * 0.0393701)}in"
        }
    }

    fun formatPressure(
        hpa: Double,
        unit: PressureUnit
    ): String {
        return when (unit) {
            PressureUnit.HPA -> "${hpa.roundToInt()} hPa"
            PressureUnit.INHG -> "${String.format(Locale.US, "%.2f", hpa * 0.02953)} inHg"
        }
    }

    fun formatLocationDate(
        utcOffsetSeconds: Int,
        timezone: String?
    ): String {
        val zoneId = try {
            if (!timezone.isNullOrBlank() && timezone != "auto") {
                ZoneId.of(timezone)
            } else {
                ZoneOffset.ofTotalSeconds(utcOffsetSeconds)
            }
        } catch (_: Exception) {
            ZoneOffset.ofTotalSeconds(utcOffsetSeconds)
        }
        val zonedDateTime = Instant.now().atZone(zoneId)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        return zonedDateTime.format(formatter)
    }

    fun formatHourLabel(
        timeIso: String,
        isFirst: Boolean,
        timeFormat: TimeFormatPreference
    ): String {
        if (isFirst) return "Now"
        return try {
            val localTime = LocalDateTime.parse(timeIso)
            when (timeFormat) {
                TimeFormatPreference.H12 -> {
                    localTime.format(DateTimeFormatter.ofPattern("h a", Locale.getDefault()))
                }
                TimeFormatPreference.H24 -> {
                    localTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                }
                TimeFormatPreference.SYSTEM -> {
                    localTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                }
            }
        } catch (_: Exception) {
            timeIso.takeLast(5)
        }
    }
}
