package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.TimeFormatPreference
import com.example.domain.model.WindSpeedUnit
import com.example.ui.components.GlassCard
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily
import com.example.ui.theme.WeatherBgDark

@Composable
fun SettingsScreen(
    uiState: WeatherUiState,
    onBack: () -> Unit,
    onSetTemperatureUnit: (TemperatureUnit) -> Unit,
    onSetWindSpeedUnit: (WindSpeedUnit) -> Unit,
    onSetPrecipitationUnit: (PrecipitationUnit) -> Unit,
    onSetPressureUnit: (PressureUnit) -> Unit,
    onSetTimeFormat: (TimeFormatPreference) -> Unit,
    onSetRainAlerts: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val prefs = uiState.userPreferences
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WeatherBgDark)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.settings_title),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Units Section
        Text(
            text = stringResource(R.string.settings_units_header),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Temperature
                Text(
                    text = stringResource(R.string.settings_temp_unit),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    color = TextWhiteSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = prefs.temperatureUnit == TemperatureUnit.CELSIUS,
                        onClick = { onSetTemperatureUnit(TemperatureUnit.CELSIUS) },
                        label = { Text("°C Celsius") },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = prefs.temperatureUnit == TemperatureUnit.FAHRENHEIT,
                        onClick = { onSetTemperatureUnit(TemperatureUnit.FAHRENHEIT) },
                        label = { Text("°F Fahrenheit") },
                        colors = chipColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wind Speed
                Text(
                    text = stringResource(R.string.settings_wind_unit),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    color = TextWhiteSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = prefs.windSpeedUnit == WindSpeedUnit.KMH,
                        onClick = { onSetWindSpeedUnit(WindSpeedUnit.KMH) },
                        label = { Text("km/h") },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = prefs.windSpeedUnit == WindSpeedUnit.MPH,
                        onClick = { onSetWindSpeedUnit(WindSpeedUnit.MPH) },
                        label = { Text("mph") },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = prefs.windSpeedUnit == WindSpeedUnit.MS,
                        onClick = { onSetWindSpeedUnit(WindSpeedUnit.MS) },
                        label = { Text("m/s") },
                        colors = chipColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Precipitation
                Text(
                    text = stringResource(R.string.settings_precip_unit),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    color = TextWhiteSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = prefs.precipitationUnit == PrecipitationUnit.MM,
                        onClick = { onSetPrecipitationUnit(PrecipitationUnit.MM) },
                        label = { Text("Millimeters (mm)") },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = prefs.precipitationUnit == PrecipitationUnit.INCH,
                        onClick = { onSetPrecipitationUnit(PrecipitationUnit.INCH) },
                        label = { Text("Inches (in)") },
                        colors = chipColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pressure
                Text(
                    text = stringResource(R.string.settings_pressure_unit),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    color = TextWhiteSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = prefs.pressureUnit == PressureUnit.HPA,
                        onClick = { onSetPressureUnit(PressureUnit.HPA) },
                        label = { Text("hPa") },
                        colors = chipColors()
                    )
                    FilterChip(
                        selected = prefs.pressureUnit == PressureUnit.INHG,
                        onClick = { onSetPressureUnit(PressureUnit.INHG) },
                        label = { Text("inHg") },
                        colors = chipColors()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time Format Section
        Text(
            text = stringResource(R.string.settings_time_format),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = prefs.timeFormat == TimeFormatPreference.SYSTEM,
                    onClick = { onSetTimeFormat(TimeFormatPreference.SYSTEM) },
                    label = { Text("System Default") },
                    colors = chipColors()
                )
                FilterChip(
                    selected = prefs.timeFormat == TimeFormatPreference.H12,
                    onClick = { onSetTimeFormat(TimeFormatPreference.H12) },
                    label = { Text("12-Hour") },
                    colors = chipColors()
                )
                FilterChip(
                    selected = prefs.timeFormat == TimeFormatPreference.H24,
                    onClick = { onSetTimeFormat(TimeFormatPreference.H24) },
                    label = { Text("24-Hour") },
                    colors = chipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notifications
        Text(
            text = stringResource(R.string.settings_notifications_header),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_rain_notifications),
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextWhitePrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_rain_notifications_desc),
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextWhiteMuted
                    )
                }
                Switch(
                    checked = prefs.rainAlertsEnabled,
                    onCheckedChange = onSetRainAlerts,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF38BDF8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data Sources & Attribution
        Text(
            text = stringResource(R.string.settings_attribution_header),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard {
            Column {
                Text(
                    text = stringResource(R.string.settings_attribution_text),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    color = TextWhiteSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "https://open-meteo.com/",
                    fontFamily = UrbanistFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cache Management
        Button(
            onClick = onClearCache,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("clear_cache_button")
        ) {
            Text(
                text = stringResource(R.string.settings_clear_cache),
                color = Color.White,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_version),
            fontFamily = UrbanistFontFamily,
            fontSize = 13.sp,
            color = TextWhiteMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Color(0xFF38BDF8),
    selectedLabelColor = Color(0xFF0F172A),
    containerColor = Color(0x331E293B),
    labelColor = Color.White
)
