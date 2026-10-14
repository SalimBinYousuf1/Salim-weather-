package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CachedForecastEntity
import com.example.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM saved_locations ORDER BY isCurrentLocation DESC, displayOrder ASC, id ASC")
    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY isCurrentLocation DESC, displayOrder ASC, id ASC")
    suspend fun getAllSavedLocationsList(): List<SavedLocationEntity>

    @Query("SELECT * FROM saved_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("DELETE FROM saved_locations WHERE isCurrentLocation = 1")
    suspend fun removeCurrentLocation()

    @Query("SELECT * FROM cached_forecasts WHERE locationKey = :locationKey")
    suspend fun getCachedForecast(locationKey: String): CachedForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCachedForecast(entity: CachedForecastEntity)

    @Query("DELETE FROM cached_forecasts WHERE locationKey = :locationKey")
    suspend fun deleteCachedForecast(locationKey: String)

    @Query("DELETE FROM cached_forecasts")
    suspend fun clearAllCachedForecasts()
}
