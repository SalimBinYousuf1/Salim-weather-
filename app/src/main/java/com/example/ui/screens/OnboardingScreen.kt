package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.WeatherLocation
import com.example.ui.components.GlassCard
import com.example.ui.scene.SceneParams
import com.example.ui.scene.WeatherSceneEngine
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.UrbanistFontFamily
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
@Composable
fun OnboardingScreen(
    uiState: WeatherUiState,
    onGpsLocationAcquired: (Double, Double) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddLocation: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showManualSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun requestGps() {
        isLocating = true
        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                isLocating = false
                onGpsLocationAcquired(loc.latitude, loc.longitude)
            } else {
                // Fallback to LocationManager
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val fallbackLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                isLocating = false
                if (fallbackLoc != null) {
                    onGpsLocationAcquired(fallbackLoc.latitude, fallbackLoc.longitude)
                } else {
                    showManualSearch = true
                }
            }
        }.addOnFailureListener {
            isLocating = false
            showManualSearch = true
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            requestGps()
        } else {
            showManualSearch = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Atmospheric Scene Background
        WeatherSceneEngine(sceneParams = SceneParams())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_title),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = TextWhitePrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_subtitle),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = TextWhiteSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!showManualSearch) {
                if (isLocating) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Acquiring your location...",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 14.sp,
                        color = TextWhiteSecondary
                    )
                } else {
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("use_location_button")
                    ) {
                        Text(
                            text = stringResource(R.string.btn_use_current_location),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showManualSearch = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("search_manually_button")
                    ) {
                        Text(
                            text = stringResource(R.string.btn_search_manually),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Manual City Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChange(it)
                    },
                    placeholder = {
                        Text(stringResource(R.string.search_city_hint), color = TextWhiteMuted)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextWhiteMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedContainerColor = Color(0x441E293B),
                        unfocusedContainerColor = Color(0x441E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_search_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Quick Suggestions",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Lagos", "Kano", "Abuja", "London").forEach { city ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0x3338BDF8))
                                    .clickable {
                                        searchQuery = city
                                        onSearchQueryChange(city)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = city,
                                    fontFamily = UrbanistFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.isSearching) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                } else if (searchQuery.isNotEmpty() && uiState.searchResults.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    ) {
                        items(uiState.searchResults) { result ->
                            GlassCard(onClick = { onAddLocation(result) }) {
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
                                        contentDescription = "Select City",
                                        tint = Color(0xFF38BDF8)
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
