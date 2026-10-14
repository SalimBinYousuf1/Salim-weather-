# Salim Weather

A native Android weather application built with modern Kotlin and Jetpack Compose. Salim Weather delivers accurate real-time forecasts, atmospheric scenes, and a frosted glass interface powered exclusively by Open-Meteo.

## Features

- **Live Meteorological Data:** Real-time conditions, 48-hour hourly forecast, 10-day daily outlook, and air quality telemetry (US AQI, PM2.5, PM10) from [Open-Meteo](https://open-meteo.com/).
- **Procedural Animated Scene Engine:** Three-layer dynamic visual canvas (sky, midground mountains/trees, and foreground water/ripples) reflecting live atmospheric parameters:
  - Dynamic sun with rotating rays and caustic shimmer
  - Dark teal-gray thunderstorm skies with branching lightning bolts
  - Parallax rain layers slanted by wind speed and direction, with surface ripple rings
  - Drifting volumetric clouds and fog banks
  - Gentle swaying snow particles
  - Night variants with twinkling starfields, lunar phases, and soft glows
- **Frosted Glass Design System:**
  - Designed for high readability meeting WCAG AA contrast on both bright daytime and stormy night skies
  - Top Bar with location pin, city/country, localized date in the city's timezone, and 40dp translucent circular menu button
  - White condition pill with black bold text
  - Large vertical gradient temperature numeral with hollow circular degree ring
  - Rule-based contextual friendly sentence
  - 3-column stats card (Humidity, Wind, Precipitation)
  - Horizontally scrolling hourly forecast card with 40dp translucent discs and colored weather vector glyphs
  - Extended forecast cards (10-day outlook with range bars, details, air quality, sun & daylight)
- **Multi-Location Support:** Smooth horizontal pager over current GPS location and saved cities with page indicator dots.
- **Manage Locations:** Real-time debounced search powered by Open-Meteo Geocoding API with region and country tags, easy addition, and swipe/tap deletion.
- **Privacy First & Zero Trackers:** No analytics, no ads, no background tracking. Network traffic is strictly limited to Open-Meteo and platform geocoding fallback.
- **Offline Resilient:** Room database caches forecasts for instant app launch with "Updated" status and stale indicator.
- **Unit Preferences:** Customizable units for Temperature (°C/°F), Wind Speed (km/h, mph, m/s), Precipitation (mm, in), Pressure (hPa, inHg), and Time Format (System, 12h, 24h).

## Architecture

- **Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Language & Framework:** 100% Kotlin & Jetpack Compose
- **Data Layer:** Retrofit + Moshi, OkHttp with on-disk HTTP caching, Room Database for persistent cache, Jetpack DataStore for preferences
- **Concurrency:** Kotlin Coroutines and StateFlow
- **Fonts:** Locally bundled Urbanist (OFL)

## Data Sources & Attribution

- Weather forecasts, geocoding, and air quality provided by [Open-Meteo.com](https://open-meteo.com/) under the [Creative Commons Attribution 4.0 International (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/) license.
- Typography: Urbanist under the SIL Open Font License (OFL) 1.1.

## Building the Project

Run standard Gradle tasks:

```bash
# Debug APK
gradle assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Release APK
gradle assembleRelease
```
