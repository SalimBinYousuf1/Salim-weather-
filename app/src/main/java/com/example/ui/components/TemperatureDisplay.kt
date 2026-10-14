package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrentWeather
import com.example.domain.model.HourlyForecastItem
import com.example.domain.model.TemperatureUnit
import com.example.domain.rules.SentenceSelector
import com.example.domain.util.UnitConverter
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun TemperatureDisplay(
    current: CurrentWeather,
    hourly: List<HourlyForecastItem>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val tempNumStr = UnitConverter.formatTemperature(
        celsius = current.temperature,
        unit = tempUnit,
        includeDegreeSign = false
    )

    val sentenceRes = SentenceSelector.selectSentenceRes(
        current = current,
        nextHours = hourly
    )

    Column(
        modifier = modifier.testTag("temperature_block"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperature Numeral with vertical gradient + hollow degree ring
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = tempNumStr,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 150.sp,
                lineHeight = 150.sp,
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFFFFF).copy(alpha = 0.85f),
                            Color(0xFFFFFFFF).copy(alpha = 0.20f)
                        )
                    )
                ),
                modifier = Modifier.testTag("temperature_value")
            )

            // Hollow Degree Ring
            Box(
                modifier = Modifier
                    .padding(top = 18.dp, start = 6.dp)
                    .size(16.dp)
                    .border(2.5.dp, Color.White, CircleShape)
                    .testTag("degree_ring")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sentence line
        Text(
            text = stringResource(sentenceRes),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = TextWhitePrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("sentence_line")
        )
    }
}
