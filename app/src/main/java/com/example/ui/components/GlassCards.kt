package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.domain.model.AirQuality
import com.example.domain.model.CurrentWeather
import com.example.domain.model.DailyForecastItem
import com.example.domain.model.HourlyForecastItem
import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.TimeFormatPreference
import com.example.domain.model.WindSpeedUnit
import com.example.domain.util.UnitConverter
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassDivider
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassCardBackground)
            .border(1.dp, GlassCardBorder, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun WeatherStatsCard(
    current: CurrentWeather,
    windUnit: WindSpeedUnit,
    precipUnit: PrecipitationUnit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassCardBackground)
            .border(1.dp, GlassCardBorder, shape)
            .clickable(onClick = onCardClick)
            .padding(vertical = 16.dp, horizontal = 12.dp)
            .testTag("stats_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Humidity Column
            StatColumn(
                icon = { HumidityIcon(Modifier.size(24.dp)) },
                value = "${current.relativeHumidity}%",
                label = stringResource(R.string.label_humidity)
            )

            // Wind Column
            StatColumn(
                icon = { WindIcon(Modifier.size(24.dp)) },
                value = UnitConverter.formatWindSpeed(current.windSpeed, windUnit),
                label = stringResource(R.string.label_wind)
            )

            // Precipitation Column
            StatColumn(
                icon = { PrecipitationIcon(Modifier.size(24.dp)) },
                value = UnitConverter.formatPrecipitation(current.precipitation, precipUnit),
                label = stringResource(R.string.label_precipitation)
            )
        }
    }
}

@Composable
private fun StatColumn(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = TextWhitePrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextWhiteMuted
        )
    }
}

@Composable
fun HourlyForecastCard(
    hourly: List<HourlyForecastItem>,
    tempUnit: TemperatureUnit,
    timeFormat: TimeFormatPreference,
    onHourClick: (HourlyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassCardBackground)
            .border(1.dp, GlassCardBorder, shape)
            .padding(top = 16.dp, bottom = 16.dp)
            .testTag("hourly_card")
    ) {
        Column {
            Text(
                text = stringResource(R.string.title_hourly_forecast),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextWhitePrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = GlassDivider,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hourly_row"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
            ) {
                itemsIndexed(hourly.take(24)) { index, item ->
                    val hourLabel = UnitConverter.formatHourLabel(item.timeIso, index == 0, timeFormat)
                    val tempStr = UnitConverter.formatTemperature(item.temperature, tempUnit, includeDegreeSign = false)

                    Column(
                        modifier = Modifier
                            .clickable { onHourClick(item) }
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = hourLabel,
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextWhiteSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        HourlyWeatherGlyphDisc(
                            weatherCode = item.weatherCode,
                            isDay = item.isDay,
                            discSize = 40.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = tempStr,
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "°",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextWhiteSecondary,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastCard(
    daily: List<DailyForecastItem>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val weekMin = daily.minOfOrNull { it.tempMin } ?: 0.0
    val weekMax = daily.maxOfOrNull { it.tempMax } ?: 40.0
    val totalRange = (weekMax - weekMin).coerceAtLeast(1.0)

    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.title_daily_forecast),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            daily.take(10).forEach { day ->
                val startFraction = ((day.tempMin - weekMin) / totalRange).coerceIn(0.0, 1.0).toFloat()
                val endFraction = ((day.tempMax - weekMin) / totalRange).coerceIn(0.0, 1.0).toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.dayLabel,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextWhitePrimary,
                        modifier = Modifier.width(65.dp)
                    )

                    HourlyWeatherGlyphDisc(
                        weatherCode = day.weatherCode,
                        isDay = true,
                        discSize = 34.dp
                    )

                    if (day.precipitationProbabilityMax > 0) {
                        Text(
                            text = "${day.precipitationProbabilityMax}%",
                            fontFamily = UrbanistFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.width(42.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(42.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = UnitConverter.formatTemperature(day.tempMin, tempUnit, true),
                            fontFamily = UrbanistFontFamily,
                            fontSize = 14.sp,
                            color = TextWhiteMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Real Dynamic Temperature Range Bar
                        Box(
                            modifier = Modifier
                                .width(74.dp)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        ) {
                            val barStart = 74.dp * startFraction
                            val barWidth = 74.dp * (endFraction - startFraction).coerceAtLeast(0.12f)
                            Box(
                                modifier = Modifier
                                    .padding(start = barStart)
                                    .width(barWidth)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(Color(0xFF38BDF8), Color(0xFFF59E0B))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = UnitConverter.formatTemperature(day.tempMax, tempUnit, true),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextWhitePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsCard(
    current: CurrentWeather,
    daily: List<DailyForecastItem>,
    tempUnit: TemperatureUnit,
    windUnit: WindSpeedUnit,
    pressureUnit: PressureUnit,
    modifier: Modifier = Modifier
) {
    val uvMax = daily.firstOrNull()?.uvIndexMax ?: 0.0
    val uvCategory = when {
        uvMax <= 2.0 -> "Low"
        uvMax <= 5.0 -> "Moderate"
        uvMax <= 7.0 -> "High"
        uvMax <= 10.0 -> "Very High"
        else -> "Extreme"
    }

    val windDegrees = current.windDirection
    val cardinal = when ((windDegrees / 45.0).toInt() % 8) {
        0 -> "N"
        1 -> "NE"
        2 -> "E"
        3 -> "SE"
        4 -> "S"
        5 -> "SW"
        6 -> "W"
        else -> "NW"
    }

    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.title_weather_details),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem(
                    label = stringResource(R.string.label_feels_like),
                    value = UnitConverter.formatTemperature(current.apparentTemperature, tempUnit, true),
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = stringResource(R.string.label_uv_index),
                    value = "${String.format(java.util.Locale.US, "%.1f", uvMax)} ($uvCategory)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem(
                    label = stringResource(R.string.label_pressure),
                    value = UnitConverter.formatPressure(current.surfacePressure, pressureUnit),
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = "Wind & Gusts",
                    value = "$cardinal • ${UnitConverter.formatWindSpeed(current.windGusts, windUnit)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontSize = 12.sp,
            color = TextWhiteMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = TextWhitePrimary
        )
    }
}

@Composable
fun AirQualityCard(
    airQuality: AirQuality?,
    modifier: Modifier = Modifier
) {
    if (airQuality?.usAqi == null) return

    val aqi = airQuality.usAqi
    val (statusRes, color) = when {
        aqi <= 50 -> R.string.aqi_good to Color(0xFF22C55E)
        aqi <= 100 -> R.string.aqi_moderate to Color(0xFFEAB308)
        aqi <= 150 -> R.string.aqi_sensitive to Color(0xFFF97316)
        aqi <= 200 -> R.string.aqi_unhealthy to Color(0xFFEF4444)
        aqi <= 300 -> R.string.aqi_very_unhealthy to Color(0xFFA855F7)
        else -> R.string.aqi_hazardous to Color(0xFF7E22CE)
    }

    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_air_quality),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = TextWhitePrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.25f))
                        .border(1.dp, color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(statusRes),
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$aqi",
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                airQuality.pm25?.let {
                    Text(
                        text = "PM2.5: ${String.format(java.util.Locale.US, "%.1f", it)} µg/m³",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }
                airQuality.pm10?.let {
                    Text(
                        text = "PM10: ${String.format(java.util.Locale.US, "%.1f", it)} µg/m³",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SunMoonCard(
    daily: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    val today = daily.firstOrNull() ?: return
    val sunrise = today.sunrise.takeLast(5)
    val sunset = today.sunset.takeLast(5)

    if (sunrise.isBlank() || sunset.isBlank()) return

    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.title_sun_moon),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Solar Daylight Arc
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(horizontal = 24.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h)
                    cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h)
                }
                drawPath(
                    path = path,
                    color = Color(0x44FFFFFF),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )

                // Solar position marker along mid-arc
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = 6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.18f)
                )
                drawCircle(
                    color = Color(0x55FDE047),
                    radius = 12.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.18f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_sunrise),
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextWhiteMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sunrise,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextWhitePrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_sunset),
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextWhiteMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sunset,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextWhitePrimary
                    )
                }
            }
        }
    }
}
