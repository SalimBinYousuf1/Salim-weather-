package com.example.ui.scene

import androidx.compose.ui.graphics.Color
import com.example.domain.model.CurrentWeather
import com.example.domain.rules.ConditionCategory
import com.example.domain.rules.WeatherConditionMapper

data class SceneParams(
    val category: ConditionCategory = ConditionCategory.CLEAR,
    val isDay: Boolean = true,
    val cloudCoverFraction: Float = 0.1f, // 0..1
    val rainIntensity: Float = 0f, // 0..1
    val snowIntensity: Float = 0f, // 0..1
    val windSpeedKmh: Float = 10f,
    val windDirectionDegrees: Float = 45f,
    val hasLightning: Boolean = false,
    val fogDensity: Float = 0f,
    val skyTopColor: Color = Color(0xFF1E3A8A),
    val skyBottomColor: Color = Color(0xFF60A5FA),
    val sunOrMoonAlpha: Float = 1f
) {
    companion object {
        fun fromWeather(current: CurrentWeather?): SceneParams {
            if (current == null) {
                return SceneParams(
                    category = ConditionCategory.CLEAR,
                    isDay = true,
                    skyTopColor = Color(0xFF1E3A8A),
                    skyBottomColor = Color(0xFF60A5FA)
                )
            }

            val category = WeatherConditionMapper.getCategory(current.weatherCode)
            val isDay = current.isDay
            val cloudFraction = (current.cloudCover / 100f).coerceIn(0f, 1f)
            val rainIntensity = when (category) {
                ConditionCategory.DRIZZLE -> 0.25f
                ConditionCategory.RAIN -> 0.65f
                ConditionCategory.HEAVY_RAIN -> 1.0f
                ConditionCategory.FREEZING_RAIN -> 0.5f
                ConditionCategory.THUNDERSTORM -> 0.9f
                else -> if (current.precipitation > 0) (current.precipitation.toFloat() / 5f).coerceIn(0.1f, 1f) else 0f
            }
            val snowIntensity = if (category == ConditionCategory.SNOW) 0.8f else 0f
            val fogDensity = if (category == ConditionCategory.FOG) 0.75f else 0f
            val hasLightning = category == ConditionCategory.THUNDERSTORM

            // Palettes matching the image
            val (skyTop, skyBottom) = when {
                !isDay -> {
                    // Night sky
                    Color(0xFF030712) to Color(0xFF0F172A)
                }
                category == ConditionCategory.THUNDERSTORM || category == ConditionCategory.HEAVY_RAIN -> {
                    // Left phone: dark teal-gray stormy sky
                    Color(0xFF111827) to Color(0xFF1E293B)
                }
                category == ConditionCategory.RAIN || category == ConditionCategory.DRIZZLE || category == ConditionCategory.FREEZING_RAIN -> {
                    // Rainy sky
                    Color(0xFF1F2937) to Color(0xFF334155)
                }
                category == ConditionCategory.OVERCAST || category == ConditionCategory.CLOUDY -> {
                    // Right phone: large volumetric clouds, cooler muted palette
                    Color(0xFF334155) to Color(0xFF64748B)
                }
                category == ConditionCategory.FOG -> {
                    Color(0xFF475569) to Color(0xFF94A3B8)
                }
                category == ConditionCategory.SNOW -> {
                    Color(0xFF334155) to Color(0xFF94A3B8)
                }
                else -> {
                    // Middle phone: deep azure/blue gradient, bright sun
                    Color(0xFF0284C7) to Color(0xFF38BDF8)
                }
            }

            return SceneParams(
                category = category,
                isDay = isDay,
                cloudCoverFraction = cloudFraction,
                rainIntensity = rainIntensity,
                snowIntensity = snowIntensity,
                windSpeedKmh = current.windSpeed.toFloat(),
                windDirectionDegrees = current.windDirection.toFloat(),
                hasLightning = hasLightning,
                fogDensity = fogDensity,
                skyTopColor = skyTop,
                skyBottomColor = skyBottom,
                sunOrMoonAlpha = (1f - cloudFraction * 0.75f).coerceIn(0.2f, 1f)
            )
        }
    }
}
