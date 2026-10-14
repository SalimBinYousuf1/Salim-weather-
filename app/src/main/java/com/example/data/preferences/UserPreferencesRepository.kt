package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.TimeFormatPreference
import com.example.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_weather_prefs")

data class UserPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MM,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val timeFormat: TimeFormatPreference = TimeFormatPreference.SYSTEM,
    val rainAlertsEnabled: Boolean = true,
    val useGpsLocation: Boolean = true,
    val onboardingCompleted: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        val KEY_PRECIP_UNIT = stringPreferencesKey("precip_unit")
        val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
        val KEY_RAIN_ALERTS = booleanPreferencesKey("rain_alerts")
        val KEY_USE_GPS = booleanPreferencesKey("use_gps")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            temperatureUnit = runCatching {
                TemperatureUnit.valueOf(preferences[PreferencesKeys.KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name)
            }.getOrDefault(TemperatureUnit.CELSIUS),
            windSpeedUnit = runCatching {
                WindSpeedUnit.valueOf(preferences[PreferencesKeys.KEY_WIND_UNIT] ?: WindSpeedUnit.KMH.name)
            }.getOrDefault(WindSpeedUnit.KMH),
            precipitationUnit = runCatching {
                PrecipitationUnit.valueOf(preferences[PreferencesKeys.KEY_PRECIP_UNIT] ?: PrecipitationUnit.MM.name)
            }.getOrDefault(PrecipitationUnit.MM),
            pressureUnit = runCatching {
                PressureUnit.valueOf(preferences[PreferencesKeys.KEY_PRESSURE_UNIT] ?: PressureUnit.HPA.name)
            }.getOrDefault(PressureUnit.HPA),
            timeFormat = runCatching {
                TimeFormatPreference.valueOf(preferences[PreferencesKeys.KEY_TIME_FORMAT] ?: TimeFormatPreference.SYSTEM.name)
            }.getOrDefault(TimeFormatPreference.SYSTEM),
            rainAlertsEnabled = preferences[PreferencesKeys.KEY_RAIN_ALERTS] ?: true,
            useGpsLocation = preferences[PreferencesKeys.KEY_USE_GPS] ?: true,
            onboardingCompleted = preferences[PreferencesKeys.KEY_ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[PreferencesKeys.KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        context.dataStore.edit { it[PreferencesKeys.KEY_WIND_UNIT] = unit.name }
    }

    suspend fun setPrecipitationUnit(unit: PrecipitationUnit) {
        context.dataStore.edit { it[PreferencesKeys.KEY_PRECIP_UNIT] = unit.name }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        context.dataStore.edit { it[PreferencesKeys.KEY_PRESSURE_UNIT] = unit.name }
    }

    suspend fun setTimeFormat(format: TimeFormatPreference) {
        context.dataStore.edit { it[PreferencesKeys.KEY_TIME_FORMAT] = format.name }
    }

    suspend fun setRainAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_RAIN_ALERTS] = enabled }
    }

    suspend fun setUseGpsLocation(useGps: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_USE_GPS] = useGps }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_ONBOARDING_COMPLETED] = completed }
    }
}
