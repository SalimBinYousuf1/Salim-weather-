package com.example.domain.rules

import com.example.R

enum class ConditionCategory {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    FREEZING_RAIN
}

object WeatherConditionMapper {

    fun getCategory(weatherCode: Int): ConditionCategory {
        return when (weatherCode) {
            0 -> ConditionCategory.CLEAR
            1 -> ConditionCategory.MAINLY_CLEAR
            2 -> ConditionCategory.PARTLY_CLOUDY
            3 -> ConditionCategory.OVERCAST
            45, 48 -> ConditionCategory.FOG
            51, 53, 55 -> ConditionCategory.DRIZZLE
            56, 57 -> ConditionCategory.FREEZING_RAIN
            61, 63 -> ConditionCategory.RAIN
            65 -> ConditionCategory.HEAVY_RAIN
            66, 67 -> ConditionCategory.FREEZING_RAIN
            71, 73, 75, 77 -> ConditionCategory.SNOW
            80, 81 -> ConditionCategory.RAIN
            82 -> ConditionCategory.HEAVY_RAIN
            85, 86 -> ConditionCategory.SNOW
            95, 96, 99 -> ConditionCategory.THUNDERSTORM
            else -> ConditionCategory.PARTLY_CLOUDY
        }
    }

    fun getConditionPillTextRes(weatherCode: Int, isDay: Boolean): Int {
        return when (weatherCode) {
            0 -> if (isDay) R.string.cond_sunny else R.string.cond_clear_night
            1 -> if (isDay) R.string.cond_mostly_sunny else R.string.cond_clear_night
            2 -> R.string.cond_partly_cloudy
            3 -> R.string.cond_overcast
            45, 48 -> R.string.cond_foggy
            51, 53, 55 -> R.string.cond_drizzle
            56, 57 -> R.string.cond_freezing_rain
            61, 63 -> R.string.cond_rainy
            65 -> R.string.cond_heavy_rain
            66, 67 -> R.string.cond_freezing_rain
            71, 73, 75, 77 -> R.string.cond_snowy
            80, 81 -> R.string.cond_rainy
            82 -> R.string.cond_heavy_rain
            85, 86 -> R.string.cond_snowy
            95, 96, 99 -> R.string.cond_thunderstorm
            else -> R.string.cond_partly_cloudy
        }
    }
}
