package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.CurrentWeather
import com.example.domain.model.PrecipitationUnit
import com.example.domain.model.PressureUnit
import com.example.domain.model.TemperatureUnit
import com.example.domain.model.WindSpeedUnit
import com.example.domain.rules.ConditionCategory
import com.example.domain.rules.SentenceSelector
import com.example.domain.rules.WeatherConditionMapper
import com.example.domain.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim Weather", appName)
    }

    @Test
    fun `test temperature conversion`() {
        assertEquals("20°", UnitConverter.formatTemperature(20.0, TemperatureUnit.CELSIUS, true))
        assertEquals("68°", UnitConverter.formatTemperature(20.0, TemperatureUnit.FAHRENHEIT, true))
        assertEquals("0", UnitConverter.formatTemperature(0.0, TemperatureUnit.CELSIUS, false))
        assertEquals("32", UnitConverter.formatTemperature(0.0, TemperatureUnit.FAHRENHEIT, false))
    }

    @Test
    fun `test wind speed conversion`() {
        assertEquals("14km/h", UnitConverter.formatWindSpeed(14.0, WindSpeedUnit.KMH))
        assertEquals("9mph", UnitConverter.formatWindSpeed(14.0, WindSpeedUnit.MPH))
    }

    @Test
    fun `test precipitation formatting`() {
        assertEquals("3mm", UnitConverter.formatPrecipitation(3.0, PrecipitationUnit.MM))
        assertEquals("0.12in", UnitConverter.formatPrecipitation(3.0, PrecipitationUnit.INCH))
    }

    @Test
    fun `test pressure formatting`() {
        assertEquals("1013 hPa", UnitConverter.formatPressure(1013.25, PressureUnit.HPA))
    }

    @Test
    fun `test weather code mapping`() {
        assertEquals(ConditionCategory.CLEAR, WeatherConditionMapper.getCategory(0))
        assertEquals(ConditionCategory.MAINLY_CLEAR, WeatherConditionMapper.getCategory(1))
        assertEquals(ConditionCategory.PARTLY_CLOUDY, WeatherConditionMapper.getCategory(2))
        assertEquals(ConditionCategory.OVERCAST, WeatherConditionMapper.getCategory(3))
        assertEquals(ConditionCategory.RAIN, WeatherConditionMapper.getCategory(61))
        assertEquals(ConditionCategory.HEAVY_RAIN, WeatherConditionMapper.getCategory(65))
        assertEquals(ConditionCategory.THUNDERSTORM, WeatherConditionMapper.getCategory(95))
        assertEquals(ConditionCategory.SNOW, WeatherConditionMapper.getCategory(71))
    }

    @Test
    fun `test sentence selector for rain`() {
        val currentRain = CurrentWeather(
            temperature = 22.0,
            apparentTemperature = 22.0,
            relativeHumidity = 85,
            precipitation = 2.5,
            weatherCode = 61,
            cloudCover = 90,
            surfacePressure = 1012.0,
            windSpeed = 12.0,
            windDirection = 180.0,
            windGusts = 18.0,
            isDay = true,
            timestampSeconds = 1700000000L
        )
        val sentenceRes = SentenceSelector.selectSentenceRes(
            current = currentRain,
            nextHours = emptyList(),
            date = LocalDate.of(2026, 10, 14)
        )
        assertNotNull(sentenceRes)
    }
}
