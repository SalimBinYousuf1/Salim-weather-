package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.rules.ConditionCategory
import com.example.domain.rules.WeatherConditionMapper
import com.example.ui.theme.GlassDiscBackground

@Composable
fun HourlyWeatherGlyphDisc(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    discSize: Dp = 40.dp
) {
    val category = WeatherConditionMapper.getCategory(weatherCode)

    Box(
        modifier = modifier
            .size(discSize)
            .clip(CircleShape)
            .background(GlassDiscBackground),
        contentAlignment = Alignment.Center
    ) {
        when (category) {
            ConditionCategory.CLEAR -> {
                if (isDay) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sunny",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "Clear Night",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            ConditionCategory.MAINLY_CLEAR, ConditionCategory.PARTLY_CLOUDY -> {
                if (isDay) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Partly Cloudy",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "Partly Cloudy Night",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            ConditionCategory.CLOUDY, ConditionCategory.OVERCAST, ConditionCategory.FOG -> {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Cloudy",
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier.size(20.dp)
                )
            }
            ConditionCategory.DRIZZLE, ConditionCategory.RAIN, ConditionCategory.FREEZING_RAIN -> {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Rain",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }
            ConditionCategory.HEAVY_RAIN -> {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Heavy Rain",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(22.dp)
                )
            }
            ConditionCategory.THUNDERSTORM -> {
                Icon(
                    imageVector = Icons.Default.Thunderstorm,
                    contentDescription = "Thunderstorm",
                    tint = Color(0xFFFCD34D),
                    modifier = Modifier.size(22.dp)
                )
            }
            ConditionCategory.SNOW -> {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = "Snow",
                    tint = Color(0xFFE0F2FE),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HumidityIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.WaterDrop,
        contentDescription = "Humidity",
        tint = Color.White,
        modifier = modifier
    )
}

@Composable
fun WindIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.Air,
        contentDescription = "Wind",
        tint = Color.White,
        modifier = modifier
    )
}

@Composable
fun PrecipitationIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.Cloud,
        contentDescription = "Precipitation",
        tint = Color.White,
        modifier = modifier
    )
}
