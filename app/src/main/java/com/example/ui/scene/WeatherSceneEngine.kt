package com.example.ui.scene

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.rules.ConditionCategory
import kotlin.math.sin
import kotlin.random.Random

private class RainParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var length: Float,
    var alpha: Float,
    var layer: Int
)

private class RippleRing(
    var xRatio: Float,
    var yRatio: Float,
    var radius: Float,
    var maxRadius: Float,
    var alpha: Float
)

@Composable
fun WeatherSceneEngine(
    sceneParams: SceneParams,
    modifier: Modifier = Modifier
) {
    val animatedTopColor by animateColorAsState(
        targetValue = sceneParams.skyTopColor,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "skyTop"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = sceneParams.skyBottomColor,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "skyBottom"
    )
    val animatedRainIntensity by animateFloatAsState(
        targetValue = sceneParams.rainIntensity,
        animationSpec = tween(800),
        label = "rain"
    )
    val animatedSnowIntensity by animateFloatAsState(
        targetValue = sceneParams.snowIntensity,
        animationSpec = tween(800),
        label = "snow"
    )
    val animatedCloudFraction by animateFloatAsState(
        targetValue = sceneParams.cloudCoverFraction,
        animationSpec = tween(800),
        label = "cloud"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "weatherLoop")

    // Continuous time ticker (0..100000)
    val timeProgression by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(600000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeProgress"
    )

    // Lightning cycle
    val lightningProgression by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightningProgress"
    )

    // Compute instantaneous lightning flash strength
    val lightningFlash = if (sceneParams.hasLightning && lightningProgression in 0.48f..0.52f) {
        (sin((lightningProgression - 0.48f) / 0.04f * Math.PI.toFloat()) * 0.9f).coerceIn(0f, 1f)
    } else 0f

    // AGSL RuntimeShader instance (API 33+)
    val agslRuntimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { AgslAtmosphereShader.createShader() }.getOrNull()
        } else null
    }

    val shaderBrush = remember(agslRuntimeShader) {
        agslRuntimeShader?.let { ShaderBrush(it) }
    }

    // Pre-allocated particle pools
    val rainParticles = remember {
        val random = Random(42)
        List(120) {
            RainParticle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                speed = 0.5f + random.nextFloat() * 0.8f,
                length = 18f + random.nextFloat() * 30f,
                alpha = 0.25f + random.nextFloat() * 0.5f,
                layer = random.nextInt(3)
            )
        }
    }

    val ripples = remember {
        val random = Random(123)
        List(14) {
            RippleRing(
                xRatio = 0.1f + random.nextFloat() * 0.8f,
                yRatio = 0.84f + random.nextFloat() * 0.14f,
                radius = random.nextFloat() * 25f,
                maxRadius = 20f + random.nextFloat() * 35f,
                alpha = 0.35f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val isStorm = sceneParams.hasLightning || sceneParams.category == ConditionCategory.HEAVY_RAIN

        // 1. Draw Real AGSL Atmospheric Shader
        if (agslRuntimeShader != null && shaderBrush != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AgslAtmosphereShader.updateUniforms(
                shader = agslRuntimeShader,
                width = width,
                height = height,
                time = timeProgression,
                cloudCover = animatedCloudFraction,
                rainIntensity = animatedRainIntensity,
                stormIntensity = if (isStorm) 1f else 0f,
                lightning = lightningFlash,
                sunGlow = sceneParams.sunOrMoonAlpha,
                isDay = sceneParams.isDay,
                windSpeed = sceneParams.windSpeedKmh,
                fogDensity = sceneParams.fogDensity,
                snowIntensity = animatedSnowIntensity,
                skyTop = animatedTopColor,
                skyBottom = animatedBottomColor
            )
            drawRect(brush = shaderBrush)
        } else {
            // High-fidelity fallback for older Android versions
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedTopColor, animatedBottomColor),
                    startY = 0f,
                    endY = height
                )
            )
        }

        // 2. Physical Particle Layers: Rain & Ripples
        if (animatedRainIntensity > 0.05f) {
            drawPhysicalRain(
                width = width,
                height = height,
                particles = rainParticles,
                intensity = animatedRainIntensity,
                windSpeed = sceneParams.windSpeedKmh,
                timeProgression = timeProgression
            )
            drawPhysicalRipples(
                width = width,
                height = height,
                ripples = ripples,
                intensity = animatedRainIntensity,
                timeProgression = timeProgression
            )
        }

        // 3. Falling Snow Particles
        if (animatedSnowIntensity > 0.05f) {
            drawPhysicalSnow(
                width = width,
                height = height,
                particles = rainParticles,
                intensity = animatedSnowIntensity,
                timeProgression = timeProgression
            )
        }

        // 4. Branching Lightning Bolt Flash Overlay
        if (lightningFlash > 0.15f) {
            drawBranchingBolt(
                width = width,
                height = height,
                flashStrength = lightningFlash
            )
        }
    }
}

private fun DrawScope.drawPhysicalRain(
    width: Float,
    height: Float,
    particles: List<RainParticle>,
    intensity: Float,
    windSpeed: Float,
    timeProgression: Float
) {
    val slant = (windSpeed * 0.45f).coerceIn(-20f, 20f)
    val activeCount = (particles.size * intensity).toInt().coerceAtLeast(15)

    for (i in 0 until activeCount) {
        val p = particles[i]
        val currentY = ((p.y + timeProgression * 5.0f * p.speed) % 1f) * height
        val currentX = ((p.x * width + timeProgression * 0.8f * slant) % width).let {
            if (it < 0) it + width else it
        }

        drawLine(
            color = Color(0xFFBAE6FD).copy(alpha = p.alpha * intensity),
            start = Offset(currentX, currentY),
            end = Offset(currentX + slant, currentY + p.length),
            strokeWidth = if (p.layer == 2) 2.2f else 1.2f
        )
    }
}

private fun DrawScope.drawPhysicalRipples(
    width: Float,
    height: Float,
    ripples: List<RippleRing>,
    intensity: Float,
    timeProgression: Float
) {
    for (r in ripples) {
        val currentRadius = ((r.radius + timeProgression * 8f) % r.maxRadius)
        val alphaFraction = (1f - (currentRadius / r.maxRadius)).coerceIn(0f, 1f) * 0.35f * intensity

        drawOval(
            color = Color(0xFFBAE6FD).copy(alpha = alphaFraction),
            topLeft = Offset(width * r.xRatio - currentRadius, height * r.yRatio - (currentRadius * 0.35f)),
            size = Size(currentRadius * 2f, currentRadius * 0.7f),
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawPhysicalSnow(
    width: Float,
    height: Float,
    particles: List<RainParticle>,
    intensity: Float,
    timeProgression: Float
) {
    val activeCount = (particles.size * intensity).toInt().coerceAtLeast(20)
    for (i in 0 until activeCount) {
        val p = particles[i]
        val currentY = ((p.y + timeProgression * 1.5f * p.speed) % 1f) * height
        val sway = sin(timeProgression * 2.0f + p.x * 20f) * 14f
        val currentX = ((p.x * width + sway) % width).let { if (it < 0) it + width else it }

        drawCircle(
            color = Color.White.copy(alpha = p.alpha * intensity),
            radius = if (p.layer == 2) 3.5f else 2f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawBranchingBolt(
    width: Float,
    height: Float,
    flashStrength: Float
) {
    val startX = width * 0.58f
    val boltPath = Path().apply {
        moveTo(startX, height * 0.08f)
        lineTo(startX - 28f, height * 0.20f)
        lineTo(startX + 12f, height * 0.30f)
        lineTo(startX - 35f, height * 0.44f)
        lineTo(startX - 18f, height * 0.54f)
        lineTo(startX - 48f, height * 0.68f)
    }
    drawPath(
        boltPath,
        color = Color.White.copy(alpha = flashStrength * 0.9f),
        style = Stroke(width = 3.5f)
    )
}
