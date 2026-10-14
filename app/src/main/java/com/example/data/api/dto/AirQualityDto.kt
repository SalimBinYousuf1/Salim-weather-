package com.example.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AirQualityResponseDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: CurrentAirQualityDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "us_aqi") val usAqi: Int? = null,
    @Json(name = "european_aqi") val europeanAqi: Int? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null
)
