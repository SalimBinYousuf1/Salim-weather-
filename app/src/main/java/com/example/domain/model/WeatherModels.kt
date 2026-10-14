package com.example.domain.model

data class WeatherLocation(
    val id: Long = 0,
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto",
    val isCurrentLocation: Boolean = false,
    val displayOrder: Int = 0
)

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val relativeHumidity: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val cloudCover: Int,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val windGusts: Double,
    val isDay: Boolean,
    val timestampSeconds: Long
)

data class HourlyForecastItem(
    val timeIso: String,
    val timeLabel: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val precipitationProbability: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val relativeHumidity: Int,
    val uvIndex: Double,
    val visibilityMeters: Double,
    val isDay: Boolean
)

data class DailyForecastItem(
    val dateIso: String,
    val dayLabel: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbabilityMax: Int,
    val precipitationSum: Double,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String,
    val windSpeedMax: Double
)

data class AirQuality(
    val usAqi: Int?,
    val europeanAqi: Int?,
    val pm25: Double?,
    val pm10: Double?
)

data class FullWeatherForecast(
    val location: WeatherLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQuality? = null,
    val timezone: String,
    val utcOffsetSeconds: Int,
    val lastUpdatedMillis: Long
)

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

enum class WindSpeedUnit {
    KMH, MPH, MS
}

enum class PrecipitationUnit {
    MM, INCH
}

enum class PressureUnit {
    HPA, INHG
}

enum class TimeFormatPreference {
    SYSTEM, H12, H24
}
