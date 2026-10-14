package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.rules.WeatherConditionMapper
import com.example.ui.theme.PillBackground
import com.example.ui.theme.PillText
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun ConditionPill(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val textRes = WeatherConditionMapper.getConditionPillTextRes(weatherCode, isDay)

    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PillBackground)
            .padding(horizontal = 14.dp)
            .testTag("condition_pill"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(textRes),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = PillText
        )
    }
}
