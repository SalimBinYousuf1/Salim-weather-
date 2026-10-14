package com.example.data.network

import android.content.Context
import com.example.data.api.OpenMeteoAirQualityApi
import com.example.data.api.OpenMeteoForecastApi
import com.example.data.api.OpenMeteoGeocodingApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkClient(context: Context) {

    private val cacheDir = File(context.cacheDir, "http_cache")
    private val cacheSize = 15L * 1024 * 1024 // 15 MB

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(cacheDir, cacheSize))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "SalimWeather-Android/1.0.0 (callme.salim.bhat@gmail.com)")
                .build()
            chain.proceed(request)
        }
        .build()

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val forecastRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val airQualityRetrofit = Retrofit.Builder()
        .baseUrl("https://air-quality-api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val forecastApi: OpenMeteoForecastApi = forecastRetrofit.create(OpenMeteoForecastApi::class.java)
    val geocodingApi: OpenMeteoGeocodingApi = geocodingRetrofit.create(OpenMeteoGeocodingApi::class.java)
    val airQualityApi: OpenMeteoAirQualityApi = airQualityRetrofit.create(OpenMeteoAirQualityApi::class.java)
}
