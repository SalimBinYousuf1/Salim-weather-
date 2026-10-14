package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.FullWeatherForecast
import com.example.domain.model.HourlyForecastItem
import com.example.domain.model.WeatherLocation
import com.example.domain.rules.ConditionCategory
import com.example.domain.util.UnitConverter
import com.example.ui.components.AirQualityCard
import com.example.ui.components.ConditionPill
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.HourlyForecastCard
import com.example.ui.components.HourlyWeatherGlyphDisc
import com.example.ui.components.SunMoonCard
import com.example.ui.components.TemperatureDisplay
import com.example.ui.components.WeatherDetailsCard
import com.example.ui.components.WeatherStatsCard
import com.example.ui.components.WeatherTopBar
import com.example.ui.scene.SceneParams
import com.example.ui.scene.WeatherSceneEngine
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassDivider
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onLocationPageChanged: (Int) -> Unit,
    onNavigateToManageLocations: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locations = uiState.locations
    if (locations.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedLocationIndex.coerceIn(0, locations.lastIndex),
        pageCount = { locations.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onLocationPageChanged(pagerState.currentPage)
    }

    val currentLocation = locations.getOrNull(pagerState.currentPage) ?: locations.first()
    val currentKey = "${currentLocation.latitude},${currentLocation.longitude}"
    val currentForecast = uiState.forecasts[currentKey]

    var showMenu by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf<HourlyForecastItem?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Live Background Scene Engine - 100% directly driven by real Open-Meteo API
        val sceneParams = remember(currentForecast) {
            SceneParams.fromWeather(currentForecast?.current)
        }

        WeatherSceneEngine(sceneParams = sceneParams)

        // 2. Main Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageLocation = locations[page]
            val pageKey = "${pageLocation.latitude},${pageLocation.longitude}"
            val forecast = uiState.forecasts[pageKey]

            WeatherPageContent(
                location = pageLocation,
                forecast = forecast,
                uiState = uiState,
                onMenuClick = { showMenu = true },
                onHourClick = { item -> selectedHour = item }
            )
        }

        // 3. Pager Indicator Dots (if multiple locations)
        if (locations.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(locations.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0x66FFFFFF))
                    )
                }
            }
        }

        // 4. Loading indicator overlay
        AnimatedVisibility(
            visible = uiState.isLoading && currentForecast == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(color = Color.White)
        }

        // 5. Options Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(Color(0xFF1E293B))
                .testTag("options_menu")
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_manage_locations), color = Color.White) },
                onClick = {
                    showMenu = false
                    onNavigateToManageLocations()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_refresh), color = Color.White) },
                onClick = {
                    showMenu = false
                    onRefresh()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_settings), color = Color.White) },
                onClick = {
                    showMenu = false
                    onNavigateToSettings()
                }
            )
        }

        // 6. Interactive Hourly Telemetry Details Dialog
        if (selectedHour != null) {
            val hour = selectedHour!!
            BasicAlertDialog(
                onDismissRequest = { selectedHour = null }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HourlyWeatherGlyphDisc(
                                    weatherCode = hour.weatherCode,
                                    isDay = hour.isDay,
                                    discSize = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = hour.timeIso.replace("T", "  "),
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { selectedHour = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = Color(0x33FFFFFF))

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Temperature", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    UnitConverter.formatTemperature(hour.temperature, uiState.userPreferences.temperatureUnit, true),
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("Feels Like", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    UnitConverter.formatTemperature(hour.apparentTemperature, uiState.userPreferences.temperatureUnit, true),
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("Rain Chance", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    "${hour.precipitationProbability}%",
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Wind Speed", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    UnitConverter.formatWindSpeed(hour.windSpeed, uiState.userPreferences.windSpeedUnit),
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("Humidity", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    "${hour.relativeHumidity}%",
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("UV Index", color = TextWhiteMuted, fontSize = 12.sp)
                                Text(
                                    "${hour.uvIndex}",
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherPageContent(
    location: WeatherLocation,
    forecast: FullWeatherForecast?,
    uiState: WeatherUiState,
    onMenuClick: () -> Unit,
    onHourClick: (HourlyForecastItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar (Location + Date + 40dp Menu Button)
        WeatherTopBar(
            location = location,
            utcOffsetSeconds = forecast?.utcOffsetSeconds ?: 0,
            timezone = forecast?.timezone,
            onMenuClick = onMenuClick
        )

        // Offline / Stale Status Banner if applicable
        if (uiState.isOffline && forecast != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x55000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.offline_cached_notice),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (forecast != null) {
            // Condition Pill (centered, height 24dp)
            ConditionPill(
                weatherCode = forecast.current.weatherCode,
                isDay = forecast.current.isDay
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Temperature Block (Large numeral + hollow degree ring + sentence line)
            TemperatureDisplay(
                current = forecast.current,
                hourly = forecast.hourly,
                tempUnit = uiState.userPreferences.temperatureUnit
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Stats Glass Card (Humidity, Wind, Precipitation)
            WeatherStatsCard(
                current = forecast.current,
                windUnit = uiState.userPreferences.windSpeedUnit,
                precipUnit = uiState.userPreferences.precipitationUnit,
                onCardClick = {}
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Hourly Forecast Glass Card
            HourlyForecastCard(
                hourly = forecast.hourly,
                tempUnit = uiState.userPreferences.temperatureUnit,
                timeFormat = uiState.userPreferences.timeFormat,
                onHourClick = onHourClick
            )

            // Extended Cards below first viewport
            Spacer(modifier = Modifier.height(18.dp))
            DailyForecastCard(
                daily = forecast.daily,
                tempUnit = uiState.userPreferences.temperatureUnit
            )

            Spacer(modifier = Modifier.height(18.dp))
            WeatherDetailsCard(
                current = forecast.current,
                daily = forecast.daily,
                tempUnit = uiState.userPreferences.temperatureUnit,
                windUnit = uiState.userPreferences.windSpeedUnit,
                pressureUnit = uiState.userPreferences.pressureUnit
            )

            if (forecast.airQuality != null) {
                Spacer(modifier = Modifier.height(18.dp))
                AirQualityCard(airQuality = forecast.airQuality)
            }

            Spacer(modifier = Modifier.height(18.dp))
            SunMoonCard(daily = forecast.daily)
        } else {
            // Skeleton loader or initial fetching
            Spacer(modifier = Modifier.height(100.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}
