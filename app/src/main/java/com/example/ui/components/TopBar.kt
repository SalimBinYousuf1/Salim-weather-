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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WeatherLocation
import com.example.domain.util.UnitConverter
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun WeatherTopBar(
    location: WeatherLocation,
    utcOffsetSeconds: Int,
    timezone: String?,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = UnitConverter.formatLocationDate(utcOffsetSeconds, timezone)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = TextWhitePrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val locationTitle = if (!location.country.isNullOrBlank()) {
                    "${location.name}, ${location.country}"
                } else {
                    location.name
                }

                Text(
                    text = locationTitle,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = TextWhitePrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("location_title")
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateString,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = TextWhiteSecondary,
                modifier = Modifier.testTag("date_label")
            )
        }

        // 40dp Translucent Menu Button with 3 dots
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x2BFFFFFF))
                .border(1.dp, Color(0x38FFFFFF), CircleShape)
                .clickable(onClick = onMenuClick)
                .testTag("menu_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options Menu",
                tint = TextWhitePrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
