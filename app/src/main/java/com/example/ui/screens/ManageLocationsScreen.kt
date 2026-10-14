package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.domain.model.WeatherLocation
import com.example.domain.util.UnitConverter
import com.example.ui.components.GlassCard
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily
import com.example.ui.theme.WeatherBgDark

@Composable
fun ManageLocationsScreen(
    uiState: WeatherUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddLocation: (WeatherLocation) -> Unit,
    onDeleteLocation: (Long) -> Unit,
    onSelectLocation: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WeatherBgDark)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Navigation Row
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
                text = stringResource(R.string.menu_manage_locations),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChange(it)
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_city_hint),
                    color = TextWhiteMuted,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextWhiteMuted
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onSearchQueryChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextWhiteMuted
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedContainerColor = Color(0x331E293B),
                unfocusedContainerColor = Color(0x331E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results List (if actively searching)
        if (searchQuery.isNotEmpty()) {
            if (uiState.isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else if (uiState.searchResults.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_search_results),
                    color = TextWhiteMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "Search Results",
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.searchResults) { result ->
                        GlassCard(
                            onClick = {
                                onAddLocation(result)
                                searchQuery = ""
                                onSearchQueryChange("")
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.name,
                                        fontFamily = UrbanistFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = TextWhitePrimary
                                    )
                                    val subtitle = listOfNotNull(result.region, result.country).joinToString(", ")
                                    if (subtitle.isNotEmpty()) {
                                        Text(
                                            text = subtitle,
                                            fontFamily = UrbanistFontFamily,
                                            fontSize = 13.sp,
                                            color = TextWhiteMuted
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Location",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Saved Locations List
            Text(
                text = stringResource(R.string.saved_locations_header),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextWhiteSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.locations) { location ->
                    val key = "${location.latitude},${location.longitude}"
                    val forecast = uiState.forecasts[key]
                    val tempStr = forecast?.current?.let {
                        UnitConverter.formatTemperature(it.temperature, uiState.userPreferences.temperatureUnit, true)
                    } ?: "--°"

                    GlassCard(
                        onClick = {
                            val index = uiState.locations.indexOf(location)
                            if (index >= 0) {
                                onSelectLocation(index)
                                onBack()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (location.isCurrentLocation) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Current Location",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Column {
                                    Text(
                                        text = location.name,
                                        fontFamily = UrbanistFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = TextWhitePrimary
                                    )
                                    val regionText = location.country ?: location.region
                                    if (!regionText.isNullOrBlank()) {
                                        Text(
                                            text = regionText,
                                            fontFamily = UrbanistFontFamily,
                                            fontSize = 13.sp,
                                            color = TextWhiteMuted
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tempStr,
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 22.sp,
                                    color = TextWhitePrimary
                                )

                                if (!location.isCurrentLocation) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = { onDeleteLocation(location.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
