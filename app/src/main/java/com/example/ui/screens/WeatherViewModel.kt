package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WeatherDatabase
import com.example.data.location.LocationGeocodingHelper
import com.example.data.network.NetworkClient
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WeatherRepository
import com.example.data.repository.WeatherRepositoryImpl
import com.example.domain.model.FullWeatherForecast
import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.TimeFormatPreference
import com.example.domain.model.WeatherLocation
import com.example.domain.model.WindSpeedUnit
import com.example.domain.rules.ConditionCategory
import com.example.ui.scene.SceneParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val locations: List<WeatherLocation> = emptyList(),
    val selectedLocationIndex: Int = 0,
    val forecasts: Map<String, FullWeatherForecast> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val searchResults: List<WeatherLocation> = emptyList(),
    val isSearching: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences()
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val networkClient = NetworkClient(application)
    private val database = WeatherDatabase.getInstance(application)
    private val repository: WeatherRepository = WeatherRepositoryImpl(
        forecastApi = networkClient.forecastApi,
        geocodingApi = networkClient.geocodingApi,
        airQualityApi = networkClient.airQualityApi,
        weatherDao = database.weatherDao(),
        moshi = networkClient.moshi
    )
    private val preferencesRepository = UserPreferencesRepository(application)
    private val geocodingHelper = LocationGeocodingHelper(application)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Collect user preferences
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collectLatest { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
            }
        }

        // Collect saved locations
        viewModelScope.launch {
            repository.getSavedLocationsFlow().collectLatest { locations ->
                _uiState.update { current ->
                    val newIndex = current.selectedLocationIndex.coerceIn(0, (locations.size - 1).coerceAtLeast(0))
                    current.copy(
                        locations = locations,
                        selectedLocationIndex = newIndex
                    )
                }

                // Fetch weather for all locations
                locations.forEach { loc ->
                    loadForecastForLocation(loc, forceRefresh = false)
                }
            }
        }
    }

    fun setSelectedLocationIndex(index: Int) {
        if (index in _uiState.value.locations.indices) {
            _uiState.update { it.copy(selectedLocationIndex = index) }
        }
    }

    fun refreshCurrentLocation(force: Boolean = true) {
        val state = _uiState.value
        val currentLocation = state.locations.getOrNull(state.selectedLocationIndex)
        if (currentLocation != null) {
            loadForecastForLocation(currentLocation, forceRefresh = force)
        }
    }

    fun loadForecastForLocation(location: WeatherLocation, forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val key = "${location.latitude},${location.longitude}"

            val result = repository.getForecast(location, forceRefresh = forceRefresh)
            result.onSuccess { forecast ->
                _uiState.update { current ->
                    val updatedMap = current.forecasts.toMutableMap()
                    updatedMap[key] = forecast
                    current.copy(
                        forecasts = updatedMap,
                        isLoading = false,
                        isOffline = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isOffline = true,
                        errorMessage = err.message
                    )
                }
            }
        }
    }

    fun updateCurrentGpsLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val resolved = geocodingHelper.resolveLocationName(latitude, longitude)
            val gpsLocation = WeatherLocation(
                id = 1L,
                name = resolved.name,
                region = resolved.region,
                country = resolved.country,
                latitude = latitude,
                longitude = longitude,
                timezone = "auto",
                isCurrentLocation = true,
                displayOrder = 0
            )

            repository.saveLocation(gpsLocation)
            loadForecastForLocation(gpsLocation, forceRefresh = true)
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun searchCities(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.searchLocations(query.trim())
            result.onSuccess { list ->
                _uiState.update { it.copy(searchResults = list, isSearching = false) }
            }.onFailure {
                _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            }
        }
    }

    fun addCity(location: WeatherLocation) {
        viewModelScope.launch {
            val nextOrder = (_uiState.value.locations.maxOfOrNull { it.displayOrder } ?: 0) + 1
            val toSave = location.copy(id = 0, isCurrentLocation = false, displayOrder = nextOrder)
            val newId = repository.saveLocation(toSave)
            preferencesRepository.setOnboardingCompleted(true)
            loadForecastForLocation(toSave.copy(id = newId), forceRefresh = true)
        }
    }

    fun deleteCity(id: Long) {
        viewModelScope.launch {
            repository.deleteLocation(id)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { preferencesRepository.setTemperatureUnit(unit) }
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch { preferencesRepository.setWindSpeedUnit(unit) }
    }

    fun setPrecipitationUnit(unit: PrecipitationUnit) {
        viewModelScope.launch { preferencesRepository.setPrecipitationUnit(unit) }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch { preferencesRepository.setPressureUnit(unit) }
    }

    fun setTimeFormat(format: TimeFormatPreference) {
        viewModelScope.launch { preferencesRepository.setTimeFormat(format) }
    }

    fun setRainAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setRainAlertsEnabled(enabled) }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            refreshCurrentLocation(force = true)
        }
    }
}
