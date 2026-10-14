package com.example.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastResponseDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "utc_offset_seconds") val utcOffsetSeconds: Int = 0,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "timezone_abbreviation") val timezoneAbbreviation: String? = null,
    @Json(name = "current") val current: CurrentWeatherDto? = null,
    @Json(name = "hourly") val hourly: HourlyWeatherDto? = null,
    @Json(name = "daily") val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double = 0.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int = 0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "cloud_cover") val cloudCover: Int = 0,
    @Json(name = "surface_pressure") val surfacePressure: Double = 1013.25,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double = 0.0,
    @Json(name = "wind_direction_10m") val windDirection10m: Double = 0.0,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double = 0.0,
    @Json(name = "is_day") val isDay: Int = 1
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int> = emptyList(),
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    @Json(name = "precipitation") val precipitation: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "surface_pressure") val surfacePressure: List<Double> = emptyList(),
    @Json(name = "visibility") val visibility: List<Double> = emptyList(),
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double> = emptyList(),
    @Json(name = "uv_index") val uvIndex: List<Double> = emptyList(),
    @Json(name = "is_day") val isDay: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "sunrise") val sunrise: List<String> = emptyList(),
    @Json(name = "sunset") val sunset: List<String> = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Double> = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double> = emptyList()
)
