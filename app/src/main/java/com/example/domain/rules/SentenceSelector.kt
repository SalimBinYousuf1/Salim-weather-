package com.example.domain.rules

import com.example.R
import com.example.domain.model.CurrentWeather
import com.example.domain.model.HourlyForecastItem
import java.time.LocalDate

object WeatherSentenceRules {
    const val PRECIPITATION_ACTIVE_THRESHOLD_MM = 0.2
    const val PRECIPITATION_PROBABILITY_THRESHOLD_PERCENT = 40
    const val HIGH_TEMP_THRESHOLD_CELSIUS = 30.0
    const val LOW_TEMP_THRESHOLD_CELSIUS = 8.0
    const val HIGH_WIND_THRESHOLD_KMH = 32.0
}

object SentenceSelector {

    fun selectSentenceRes(
        current: CurrentWeather,
        nextHours: List<HourlyForecastItem>,
        date: LocalDate = LocalDate.now()
    ): Int {
        val category = WeatherConditionMapper.getCategory(current.weatherCode)
        val dayOfYear = date.dayOfYear

        // 1. Active or upcoming thunderstorm
        if (category == ConditionCategory.THUNDERSTORM) {
            return R.string.sentence_thunderstorm
        }

        // 2. Active Rain or Freezing Rain
        if (current.precipitation >= WeatherSentenceRules.PRECIPITATION_ACTIVE_THRESHOLD_MM ||
            category == ConditionCategory.RAIN ||
            category == ConditionCategory.HEAVY_RAIN ||
            category == ConditionCategory.FREEZING_RAIN
        ) {
            val rainVariants = listOf(
                R.string.sentence_rain_umbrella,
                R.string.sentence_rain_now
            )
            return rainVariants[dayOfYear % rainVariants.size]
        }

        // 3. Rain arriving within next 4 hours
        val rainComingSoon = nextHours.take(4).any {
            it.precipitationProbability >= WeatherSentenceRules.PRECIPITATION_PROBABILITY_THRESHOLD_PERCENT ||
                    it.precipitation >= WeatherSentenceRules.PRECIPITATION_ACTIVE_THRESHOLD_MM
        }
        if (rainComingSoon) {
            return R.string.sentence_rain_incoming
        }

        // 4. Snow
        if (category == ConditionCategory.SNOW) {
            return R.string.sentence_snow
        }

        // 5. Fog
        if (category == ConditionCategory.FOG) {
            return R.string.sentence_fog
        }

        // 6. High wind
        if (current.windSpeed >= WeatherSentenceRules.HIGH_WIND_THRESHOLD_KMH) {
            return R.string.sentence_windy
        }

        // 7. Temperature extremes
        if (current.temperature <= WeatherSentenceRules.LOW_TEMP_THRESHOLD_CELSIUS) {
            return R.string.sentence_cold_chilly
        }
        if (current.temperature >= WeatherSentenceRules.HIGH_TEMP_THRESHOLD_CELSIUS) {
            return R.string.sentence_hot_summer
        }

        // 8. Clear skies / sunny
        if (category == ConditionCategory.CLEAR || category == ConditionCategory.MAINLY_CLEAR) {
            val sunnyVariants = listOf(
                R.string.sentence_clear_day,
                R.string.sentence_sunny_day
            )
            return sunnyVariants[dayOfYear % sunnyVariants.size]
        }

        // 9. Cloudy / Overcast
        return R.string.sentence_cloudy_mild
    }
}
