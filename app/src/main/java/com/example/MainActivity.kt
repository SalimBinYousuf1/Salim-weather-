package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.ManageLocationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.screens.WeatherViewModel
import com.example.ui.theme.SalimWeatherTheme

enum class ScreenDestination {
    WEATHER,
    MANAGE_LOCATIONS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SalimWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A)
                ) {
                    val uiState by weatherViewModel.uiState.collectAsState()
                    var currentScreen by remember { mutableStateOf(ScreenDestination.WEATHER) }

                    // If user has no locations saved yet, show clean first-run Onboarding
                    if (uiState.locations.isEmpty() && !uiState.userPreferences.onboardingCompleted) {
                        OnboardingScreen(
                            uiState = uiState,
                            onGpsLocationAcquired = { lat, lon ->
                                weatherViewModel.updateCurrentGpsLocation(lat, lon)
                            },
                            onSearchQueryChange = { query ->
                                weatherViewModel.searchCities(query)
                            },
                            onAddLocation = { loc ->
                                weatherViewModel.addCity(loc)
                            }
                        )
                    } else {
                        when (currentScreen) {
                            ScreenDestination.WEATHER -> {
                                WeatherScreen(
                                    uiState = uiState,
                                    onRefresh = { weatherViewModel.refreshCurrentLocation(force = true) },
                                    onLocationPageChanged = { index ->
                                        weatherViewModel.setSelectedLocationIndex(index)
                                    },
                                    onNavigateToManageLocations = {
                                        currentScreen = ScreenDestination.MANAGE_LOCATIONS
                                    },
                                    onNavigateToSettings = {
                                        currentScreen = ScreenDestination.SETTINGS
                                    },
                                    onDebugSelectCondition = { category ->
                                        weatherViewModel.setDebugOverrideCategory(category)
                                    }
                                )
                            }
                            ScreenDestination.MANAGE_LOCATIONS -> {
                                ManageLocationsScreen(
                                    uiState = uiState,
                                    onBack = { currentScreen = ScreenDestination.WEATHER },
                                    onSearchQueryChange = { query ->
                                        weatherViewModel.searchCities(query)
                                    },
                                    onAddLocation = { location ->
                                        weatherViewModel.addCity(location)
                                    },
                                    onDeleteLocation = { id ->
                                        weatherViewModel.deleteCity(id)
                                    },
                                    onSelectLocation = { index ->
                                        weatherViewModel.setSelectedLocationIndex(index)
                                    }
                                )
                            }
                            ScreenDestination.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onBack = { currentScreen = ScreenDestination.WEATHER },
                                    onSetTemperatureUnit = { weatherViewModel.setTemperatureUnit(it) },
                                    onSetWindSpeedUnit = { weatherViewModel.setWindSpeedUnit(it) },
                                    onSetPrecipitationUnit = { weatherViewModel.setPrecipitationUnit(it) },
                                    onSetPressureUnit = { weatherViewModel.setPressureUnit(it) },
                                    onSetTimeFormat = { weatherViewModel.setTimeFormat(it) },
                                    onSetRainAlerts = { weatherViewModel.setRainAlertsEnabled(it) },
                                    onClearCache = { weatherViewModel.clearCache() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
