package com.example.data.repository

import com.example.data.api.OpenMeteoAirQualityApi
import com.example.data.api.OpenMeteoForecastApi
import com.example.data.api.OpenMeteoGeocodingApi
import com.example.data.local.dao.WeatherDao
import com.example.data.local.entity.CachedForecastEntity
import com.example.data.local.entity.SavedLocationEntity
import com.example.domain.model.AirQuality
import com.example.domain.model.CurrentWeather
import com.example.domain.model.DailyForecastItem
import com.example.domain.model.FullWeatherForecast
import com.example.domain.model.HourlyForecastItem
import com.example.domain.model.WeatherLocation
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

interface WeatherRepository {
    fun getSavedLocationsFlow(): Flow<List<WeatherLocation>>
    suspend fun getSavedLocationsList(): List<WeatherLocation>
    suspend fun getCurrentSavedLocation(): WeatherLocation?
    suspend fun saveLocation(location: WeatherLocation): Long
    suspend fun deleteLocation(id: Long)
    suspend fun clearCurrentLocation()
    suspend fun searchLocations(query: String): Result<List<WeatherLocation>>
    suspend fun getForecast(location: WeatherLocation, forceRefresh: Boolean): Result<FullWeatherForecast>
    suspend fun clearCache()
}

class WeatherRepositoryImpl(
    private val forecastApi: OpenMeteoForecastApi,
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val airQualityApi: OpenMeteoAirQualityApi,
    private val weatherDao: WeatherDao,
    private val moshi: Moshi
) : WeatherRepository {

    private val forecastAdapter = moshi.adapter(FullWeatherForecast::class.java)

    override fun getSavedLocationsFlow(): Flow<List<WeatherLocation>> {
        return weatherDao.getAllSavedLocations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSavedLocationsList(): List<WeatherLocation> = withContext(Dispatchers.IO) {
        weatherDao.getAllSavedLocationsList().map { it.toDomain() }
    }

    override suspend fun getCurrentSavedLocation(): WeatherLocation? = withContext(Dispatchers.IO) {
        weatherDao.getCurrentLocation()?.toDomain()
    }

    override suspend fun saveLocation(location: WeatherLocation): Long = withContext(Dispatchers.IO) {
        weatherDao.insertLocation(location.toEntity())
    }

    override suspend fun deleteLocation(id: Long) = withContext(Dispatchers.IO) {
        weatherDao.deleteLocationById(id)
    }

    override suspend fun clearCurrentLocation() = withContext(Dispatchers.IO) {
        weatherDao.removeCurrentLocation()
    }

    override suspend fun searchLocations(query: String): Result<List<WeatherLocation>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = geocodingApi.searchLocations(name = query)
            response.results?.map { dto ->
                WeatherLocation(
                    id = dto.id,
                    name = dto.name,
                    region = dto.admin1,
                    country = dto.country,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    timezone = dto.timezone ?: "auto",
                    isCurrentLocation = false
                )
            } ?: emptyList()
        }
    }

    override suspend fun getForecast(location: WeatherLocation, forceRefresh: Boolean): Result<FullWeatherForecast> =
        withContext(Dispatchers.IO) {
            val locationKey = "${String.format(Locale.US, "%.3f", location.latitude)},${String.format(Locale.US, "%.3f", location.longitude)}"
            val nowMillis = System.currentTimeMillis()
            val cached = weatherDao.getCachedForecast(locationKey)

            if (!forceRefresh && cached != null && (nowMillis - cached.lastUpdatedMillis < 10 * 60 * 1000L)) {
                runCatching {
                    val parsed = forecastAdapter.fromJson(cached.jsonForecast)
                    if (parsed != null) {
                        return@withContext Result.success(parsed.copy(location = location))
                    }
                }
            }

            // Fresh network call
            runCatching {
                val forecastDto = forecastApi.getForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timezone = "auto"
                )

                val airQualityDto = runCatching {
                    airQualityApi.getAirQuality(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }.getOrNull()

                val currentDto = forecastDto.current
                val current = CurrentWeather(
                    temperature = currentDto?.temperature2m ?: 0.0,
                    apparentTemperature = currentDto?.apparentTemperature ?: 0.0,
                    relativeHumidity = currentDto?.relativeHumidity2m ?: 0,
                    precipitation = currentDto?.precipitation ?: 0.0,
                    weatherCode = currentDto?.weatherCode ?: 0,
                    cloudCover = currentDto?.cloudCover ?: 0,
                    surfacePressure = currentDto?.surfacePressure ?: 1013.25,
                    windSpeed = currentDto?.windSpeed10m ?: 0.0,
                    windDirection = currentDto?.windDirection10m ?: 0.0,
                    windGusts = currentDto?.windGusts10m ?: 0.0,
                    isDay = (currentDto?.isDay ?: 1) == 1,
                    timestampSeconds = nowMillis / 1000
                )

                // Parse Hourly (take next 24-48 hours)
                val hourlyList = mutableListOf<HourlyForecastItem>()
                val hourlyDto = forecastDto.hourly
                if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
                    val size = hourlyDto.time.size
                    for (i in 0 until minOf(size, 48)) {
                        val timeIso = hourlyDto.time[i]
                        val timeLabel = try {
                            val dt = LocalDateTime.parse(timeIso)
                            dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                        } catch (_: Exception) {
                            timeIso.takeLast(5)
                        }
                        hourlyList.add(
                            HourlyForecastItem(
                                timeIso = timeIso,
                                timeLabel = if (i == 0) "Now" else timeLabel,
                                temperature = hourlyDto.temperature2m.getOrElse(i) { 0.0 },
                                apparentTemperature = hourlyDto.apparentTemperature.getOrElse(i) { 0.0 },
                                precipitationProbability = hourlyDto.precipitationProbability.getOrElse(i) { 0 },
                                precipitation = hourlyDto.precipitation.getOrElse(i) { 0.0 },
                                weatherCode = hourlyDto.weatherCode.getOrElse(i) { 0 },
                                windSpeed = hourlyDto.windSpeed10m.getOrElse(i) { 0.0 },
                                relativeHumidity = hourlyDto.relativeHumidity2m.getOrElse(i) { 0 },
                                uvIndex = hourlyDto.uvIndex.getOrElse(i) { 0.0 },
                                visibilityMeters = hourlyDto.visibility.getOrElse(i) { 10000.0 },
                                isDay = (hourlyDto.isDay.getOrElse(i) { 1 }) == 1
                            )
                        )
                    }
                }

                // Parse Daily (10 days)
                val dailyList = mutableListOf<DailyForecastItem>()
                val dailyDto = forecastDto.daily
                if (dailyDto != null && dailyDto.time.isNotEmpty()) {
                    val size = dailyDto.time.size
                    for (i in 0 until minOf(size, 10)) {
                        val dateIso = dailyDto.time[i]
                        val dayLabel = if (i == 0) "Today" else try {
                            val ld = LocalDate.parse(dateIso)
                            ld.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                        } catch (_: Exception) {
                            dateIso.takeLast(5)
                        }
                        dailyList.add(
                            DailyForecastItem(
                                dateIso = dateIso,
                                dayLabel = dayLabel,
                                weatherCode = dailyDto.weatherCode.getOrElse(i) { 0 },
                                tempMax = dailyDto.temperature2mMax.getOrElse(i) { 0.0 },
                                tempMin = dailyDto.temperature2mMin.getOrElse(i) { 0.0 },
                                precipitationProbabilityMax = dailyDto.precipitationProbabilityMax.getOrElse(i) { 0 },
                                precipitationSum = dailyDto.precipitationSum.getOrElse(i) { 0.0 },
                                uvIndexMax = dailyDto.uvIndexMax.getOrElse(i) { 0.0 },
                                sunrise = dailyDto.sunrise.getOrElse(i) { "" },
                                sunset = dailyDto.sunset.getOrElse(i) { "" },
                                windSpeedMax = dailyDto.windSpeed10mMax.getOrElse(i) { 0.0 }
                            )
                        )
                    }
                }

                val airQuality = airQualityDto?.current?.let { aq ->
                    AirQuality(
                        usAqi = aq.usAqi,
                        europeanAqi = aq.europeanAqi,
                        pm25 = aq.pm25,
                        pm10 = aq.pm10
                    )
                }

                val fullForecast = FullWeatherForecast(
                    location = location,
                    current = current,
                    hourly = hourlyList,
                    daily = dailyList,
                    airQuality = airQuality,
                    timezone = forecastDto.timezone ?: location.timezone,
                    utcOffsetSeconds = forecastDto.utcOffsetSeconds,
                    lastUpdatedMillis = nowMillis
                )

                // Save to Room cache
                runCatching {
                    val json = forecastAdapter.toJson(fullForecast)
                    weatherDao.saveCachedForecast(
                        CachedForecastEntity(
                            locationKey = locationKey,
                            jsonForecast = json,
                            lastUpdatedMillis = nowMillis
                        )
                    )
                }

                fullForecast
            }.recoverCatching { networkException ->
                // Fallback to cache if network call fails
                if (cached != null) {
                    val cachedForecast = forecastAdapter.fromJson(cached.jsonForecast)
                    if (cachedForecast != null) {
                        return@recoverCatching cachedForecast.copy(location = location)
                    }
                }
                throw networkException
            }
        }

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        weatherDao.clearAllCachedForecasts()
    }

    private fun SavedLocationEntity.toDomain() = WeatherLocation(
        id = id,
        name = name,
        region = region,
        country = country,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
        isCurrentLocation = isCurrentLocation,
        displayOrder = displayOrder
    )

    private fun WeatherLocation.toEntity() = SavedLocationEntity(
        id = id,
        name = name,
        region = region,
        country = country,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
        isCurrentLocation = isCurrentLocation,
        displayOrder = displayOrder
    )
}
