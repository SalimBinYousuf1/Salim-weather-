package com.example.ui.scene

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color

object AgslAtmosphereShader {

    const val SHADER_SRC = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float uCloudCover;
        uniform float uRainIntensity;
        uniform float uStormIntensity;
        uniform float uLightning;
        uniform float uSunGlow;
        uniform float uIsDay;
        uniform float uWindSpeed;
        uniform float uFogDensity;
        uniform float uSnowIntensity;
        uniform float3 uSkyTop;
        uniform float3 uSkyBottom;

        float hash(float2 p) {
            p = fract(p * float2(123.34, 456.21));
            p += dot(p, p + 45.32);
            return fract(p.x * p.y);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(
                mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x),
                u.y
            );
        }

        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float c = 0.87758;
            float s = 0.47943;
            for (int i = 0; i < 5; ++i) {
                v += a * noise(p);
                p = float2(c * p.x - s * p.y, s * p.x + c * p.y) * 2.02 + float2(10.0, 15.0);
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float aspect = uResolution.x / uResolution.y;

            // 1. Photorealistic atmospheric sky gradient
            float skyGradient = pow(clamp(uv.y, 0.0, 1.0), 0.85);
            float3 sky = mix(uSkyTop, uSkyBottom, skyGradient);

            // Cold tint during snow
            if (uSnowIntensity > 0.1) {
                sky = mix(sky, float3(0.70, 0.82, 0.95), uSnowIntensity * 0.35);
            }

            // 2. Celestial Light (Sun or Moon)
            float2 celestialPos = float2(uResolution.x * 0.76, uResolution.y * 0.22);
            float distToSun = length(fragCoord - celestialPos) / uResolution.y;

            if (uIsDay > 0.5) {
                // Photorealistic Optical Sun: Blinding specular core + Mie forward scattering bloom
                float sunCore = smoothstep(0.038, 0.015, distToSun);
                float sunInnerCorona = exp(-distToSun * 7.5) * 1.1;
                float sunOuterBloom = exp(-distToSun * 2.4) * 0.55;

                // Subtle natural anamorphic lens flare
                float flareY = abs(fragCoord.y - celestialPos.y) / uResolution.y;
                float flareX = abs(fragCoord.x - celestialPos.x) / uResolution.x;
                float lensStreak = exp(-flareY * 35.0) * exp(-flareX * 3.5) * 0.20;

                float3 coreColor = float3(1.0, 1.0, 1.0);
                float3 coronaColor = float3(1.0, 0.96, 0.82);
                float3 bloomColor = float3(1.0, 0.90, 0.70);

                float visibility = (1.0 - uCloudCover * 0.85);
                float3 totalSun = (sunCore * 2.2 * coreColor + sunInnerCorona * coronaColor + (sunOuterBloom + lensStreak) * bloomColor) * uSunGlow * visibility;
                sky += totalSun;
            } else {
                // Night sky: twinkling star field
                if (uv.y < 0.75) {
                    float2 starCoord = fragCoord * 0.075;
                    float starVal = hash(floor(starCoord));
                    if (starVal > 0.984) {
                        float twinkle = sin(uTime * 3.5 + starVal * 25.0) * 0.5 + 0.5;
                        float starIntensity = smoothstep(0.984, 1.0, starVal) * twinkle * (1.0 - uCloudCover);
                        sky += float3(0.92, 0.96, 1.0) * starIntensity;
                    }
                }
                // Lunar Disc and Soft Lunar Corona
                float moonDist = length(fragCoord - celestialPos) / uResolution.y;
                float moonDisc = smoothstep(0.048, 0.040, moonDist);
                float moonGlow = exp(-moonDist * 4.5) * 0.38 * (1.0 - uCloudCover * 0.65);
                sky += (moonDisc + moonGlow) * float3(0.90, 0.93, 1.0);
            }

            // 3. Volumetric Fractal Noise Clouds with Perspective Drift
            float cloudHeight = 1.0 - uv.y;
            float2 cloudUV = float2(uv.x * aspect * 1.6, cloudHeight * 2.4);
            float drift = uTime * (0.015 + uWindSpeed * 0.001);
            cloudUV.x += drift;

            float cNoise1 = fbm(cloudUV * 1.8);
            float cNoise2 = fbm((cloudUV + float2(0.5, 0.8)) * 3.4 - drift * 0.4);
            float cloudDensity = smoothstep(1.0 - uCloudCover * 0.90, 1.25, cNoise1 * 0.7 + cNoise2 * 0.3);

            if (cloudDensity > 0.01) {
                float3 cloudLitColor = uIsDay > 0.5 ? float3(0.94, 0.96, 0.99) : float3(0.24, 0.27, 0.34);
                float3 cloudShadowColor = uIsDay > 0.5 ? 
                    (uStormIntensity > 0.5 ? float3(0.11, 0.14, 0.19) : float3(0.35, 0.40, 0.48)) : 
                    float3(0.08, 0.10, 0.13);

                float lightFactor = clamp(dot(normalize(fragCoord - celestialPos), float2(0.0, 1.0)), 0.0, 1.0);
                float3 cloudColor = mix(cloudShadowColor, cloudLitColor, clamp(cNoise2 * 1.2 + lightFactor * 0.3, 0.0, 1.0));

                // Lightning illumination of cloud volume
                if (uLightning > 0.0) {
                    cloudColor += float3(0.85, 0.92, 1.0) * uLightning * 1.9;
                    sky += float3(0.60, 0.70, 0.90) * uLightning * 0.50;
                }

                sky = mix(sky, cloudColor, cloudDensity * clamp(uCloudCover * 1.25, 0.0, 0.96));
            }

            // 4. Distant Mountain Silhouette
            float mountainY = 0.72 + sin(uv.x * 5.8) * 0.04 + cos(uv.x * 13.5) * 0.018;
            if (uv.y > mountainY) {
                float mountainFade = smoothstep(mountainY, mountainY + 0.02, uv.y);
                float3 mountainColor = uIsDay > 0.5 ? 
                    (uStormIntensity > 0.5 ? float3(0.06, 0.08, 0.12) : float3(0.09, 0.14, 0.20)) : 
                    float3(0.03, 0.04, 0.06);
                if (uSnowIntensity > 0.1) {
                    mountainColor = mix(mountainColor, float3(0.65, 0.75, 0.88), uSnowIntensity * 0.5);
                }
                sky = mix(sky, mountainColor, mountainFade * 0.88);
            }

            // 5. Near Forest / Ridge Silhouette
            float hillY = 0.80 + sin(uv.x * 4.2 + 1.2) * 0.035;
            if (uv.y > hillY) {
                float hillFade = smoothstep(hillY, hillY + 0.015, uv.y);
                float3 hillColor = uIsDay > 0.5 ? float3(0.04, 0.05, 0.08) : float3(0.02, 0.02, 0.04);
                sky = mix(sky, hillColor, hillFade * 0.96);
            }

            // 6. Volumetric Atmospheric Fog and Mist
            if (uFogDensity > 0.05) {
                float fogWave = fbm(float2(uv.x * 3.0 + uTime * 0.03, uv.y * 2.0));
                float fogLayer = smoothstep(0.40, 0.85, uv.y) * (1.0 - smoothstep(0.85, 1.0, uv.y));
                float fogAmount = fogLayer * (0.6 + fogWave * 0.4) * uFogDensity;
                float3 fogColor = uIsDay > 0.5 ? float3(0.82, 0.86, 0.92) : float3(0.18, 0.22, 0.28);
                sky = mix(sky, fogColor, clamp(fogAmount, 0.0, 0.85));
            }

            // 7. Water Surface Shimmer & Caustics
            if (uv.y > 0.84) {
                float waterDist = (uv.y - 0.84) / 0.16;
                float wave = sin(uv.x * 38.0 + uTime * 2.8) * cos(uv.y * 32.0 + uTime * 2.1);
                float caustic = pow(clamp(wave * 0.5 + 0.5, 0.0, 1.0), 3.0) * 0.22;
                float3 waterBase = uIsDay > 0.5 ? float3(0.07, 0.20, 0.32) : float3(0.03, 0.05, 0.09);
                sky = mix(sky, waterBase + caustic, waterDist * 0.75);
            }

            // 8. Gradient Scrims for Guaranteed AA Contrast
            float topScrim = (1.0 - smoothstep(0.0, 0.22, uv.y)) * 0.42;
            float bottomScrim = smoothstep(0.70, 1.0, uv.y) * 0.48;
            sky *= (1.0 - topScrim);
            sky *= (1.0 - bottomScrim);

            return half4(sky.r, sky.g, sky.b, 1.0);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createShader(): RuntimeShader {
        return RuntimeShader(SHADER_SRC)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun updateUniforms(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float,
        cloudCover: Float,
        rainIntensity: Float,
        stormIntensity: Float,
        lightning: Float,
        sunGlow: Float,
        isDay: Boolean,
        windSpeed: Float,
        fogDensity: Float,
        snowIntensity: Float,
        skyTop: Color,
        skyBottom: Color
    ) {
        shader.setFloatUniform("uResolution", width, height)
        shader.setFloatUniform("uTime", time)
        shader.setFloatUniform("uCloudCover", cloudCover)
        shader.setFloatUniform("uRainIntensity", rainIntensity)
        shader.setFloatUniform("uStormIntensity", stormIntensity)
        shader.setFloatUniform("uLightning", lightning)
        shader.setFloatUniform("uSunGlow", sunGlow)
        shader.setFloatUniform("uIsDay", if (isDay) 1f else 0f)
        shader.setFloatUniform("uWindSpeed", windSpeed)
        shader.setFloatUniform("uFogDensity", fogDensity)
        shader.setFloatUniform("uSnowIntensity", snowIntensity)
        shader.setFloatUniform("uSkyTop", skyTop.red, skyTop.green, skyTop.blue)
        shader.setFloatUniform("uSkyBottom", skyBottom.red, skyBottom.green, skyBottom.blue)
    }
}
