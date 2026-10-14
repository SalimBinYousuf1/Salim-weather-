# Design and Architectural Decisions

## Typeface Selection
- **Selected Font:** Urbanist (OFL)
- **Rationale:** Urbanist provides geometric, clean, modern rounded curves that precisely match the numerals, headings ("Hourly Forecast"), and card text of the weather design reference. It supports dynamic font scaling and renders crisp numerals with elegant proportions.

## Color Tokens & Glass Styling
- **Glass Card Background:** Surface frosted with dark tint `rgba(20, 26, 38, 0.45)` to ensure WCAG AA contrast (minimum 4.5:1) against both bright daytime sky backgrounds and dark stormy/night scenes.
- **Card Borders:** 1dp subtle hairline border `rgba(255, 255, 255, 0.18)` with inner highlight stroke.
- **Corner Radius:** 24.dp for all primary glass cards.
- **Top Bar Translucent Menu Button:** 40.dp diameter circle, `rgba(255, 255, 255, 0.18)` fill, 1dp border, 3 white dots.
- **Condition Pill:** 24.dp height, full capsule radius, pure white background (`#FFFFFF`), pure black bold text (`#000000`).
- **Temperature Display:** 150sp-200sp display with white-to-translucent vertical gradient fill (`#FFFFFF` at top to `rgba(255, 255, 255, 0.20)` at bottom) with hollow degree ring indicator at top right.

## Architecture
- **Clean Architecture / MVVM:** Data, Domain, and UI layers.
- **Storage:** Room Database for caching weather forecasts and managing saved cities.
- **DataStore:** Persisting user preferences (temperature unit, wind speed unit, precipitation unit, pressure unit).
- **Network:** Retrofit with Moshi and OkHttp caching with cache-control headers.
- **Scene Engine:** Real AGSL (`RuntimeShader` on Android 13+) procedural shader engine rendering photorealistic volumetric sky gradients, multi-octave fractal Brownian motion (fBm) cloud structures with rim lighting and storm shadowing, photorealistic optical sun with Mie forward scattering, specular core, and lens bloom (no cartoon rays), twinkling celestial starfield, lunar corona, mountain and hill silhouettes, and caustic water shimmers. 100% directly driven by real Open-Meteo meteorological API telemetry with physical particle overlays for wind-slanted rain streaks, surface ripples, and snow.
