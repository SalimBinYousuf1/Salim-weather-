package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_forecasts")
data class CachedForecastEntity(
    @PrimaryKey
    val locationKey: String,
    val jsonForecast: String,
    val lastUpdatedMillis: Long
)
