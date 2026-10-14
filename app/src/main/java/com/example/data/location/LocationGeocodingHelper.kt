package com.example.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

data class ResolvedLocationName(
    val name: String,
    val region: String?,
    val country: String?
)

class LocationGeocodingHelper(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    private val cache = ConcurrentHashMap<String, ResolvedLocationName>()

    suspend fun resolveLocationName(latitude: Double, longitude: Double): ResolvedLocationName =
        withContext(Dispatchers.IO) {
            val key = "${String.format(Locale.US, "%.3f", latitude)},${String.format(Locale.US, "%.3f", longitude)}"
            cache[key]?.let { return@withContext it }

            // 1. Try Android Platform Geocoder
            val platformResult = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine<ResolvedLocationName?> { cont ->
                        val geocoder = Geocoder(context, Locale.getDefault())
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            if (address != null) {
                                val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                val country = address.countryName
                                if (!city.isNullOrBlank()) {
                                    cont.resume(ResolvedLocationName(city, address.adminArea, country))
                                    return@getFromLocation
                                }
                            }
                            cont.resume(null)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                        val country = address.countryName
                        if (!city.isNullOrBlank()) {
                            ResolvedLocationName(city, address.adminArea, country)
                        } else null
                    } else null
                }
            }.getOrNull()

            if (platformResult != null) {
                cache[key] = platformResult
                return@withContext platformResult
            }

            // 2. Fall back to OpenStreetMap Nominatim reverse geocoding
            val nominatimResult = runCatching {
                val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "SalimWeather-Android/1.0.0 (callme.salim.bhat@gmail.com)")
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            val address = json.optJSONObject("address")
                            val city = address?.optString("city")
                                ?: address?.optString("town")
                                ?: address?.optString("village")
                                ?: address?.optString("county")
                                ?: json.optString("name")
                            val country = address?.optString("country")
                            val state = address?.optString("state")
                            if (!city.isNullOrBlank()) {
                                ResolvedLocationName(city, state, country)
                            } else null
                        } else null
                    } else null
                }
            }.getOrNull()

            if (nominatimResult != null) {
                cache[key] = nominatimResult
                return@withContext nominatimResult
            }

            // 3. Fall back to formatted coordinates
            val coordResult = ResolvedLocationName(
                name = "${String.format(Locale.US, "%.2f", latitude)}°, ${String.format(Locale.US, "%.2f", longitude)}°",
                region = null,
                country = null
            )
            cache[key] = coordResult
            coordResult
        }
}
