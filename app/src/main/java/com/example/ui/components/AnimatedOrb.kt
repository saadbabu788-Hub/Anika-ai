package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.OrbState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedOrb(
    orbState: OrbState,
    isActive: Boolean = true,
    orbSize: Dp = 240.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    // Pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (!isActive) 0.92f else when (orbState) {
            OrbState.LISTENING -> 0.95f
            OrbState.SPEAKING -> 0.90f
            OrbState.THINKING -> 0.94f
            else -> 0.96f
        },
        targetValue = if (!isActive) 0.94f else when (orbState) {
            OrbState.LISTENING -> 1.15f
            OrbState.SPEAKING -> 1.18f
            OrbState.THINKING -> 1.06f
            else -> 1.04f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.LISTENING -> 900
                    OrbState.SPEAKING -> 650
                    OrbState.THINKING -> 1200
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING, OrbState.PROCESSING -> 3500
                    OrbState.LISTENING -> 6000
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Dynamic color selection based on state
    val coreColors = when {
        !isActive -> listOf(Color(0xFF374151), Color(0xFF1F2937))
        orbState == OrbState.ERROR -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF7F1D1D))
        orbState == OrbState.LISTENING -> listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF8B5CF6))
        orbState == OrbState.SPEAKING -> listOf(Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF6366F1))
        orbState == OrbState.THINKING -> listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF3B82F6))
        orbState == OrbState.PROCESSING -> listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6))
        else -> listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF06B6D4))
    }

    Box(
        modifier = modifier.size(orbSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.72f * pulseScale

            // 1. Outermost subtle atmospheric aura
            if (isActive) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColors.first().copy(alpha = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) 0.35f else 0.18f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.55f
                    ),
                    radius = baseRadius * 1.55f,
                    center = center
                )

                // 2. Wave resonance rings
                val waveAlpha = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) 0.65f else 0.25f
                drawCircle(
                    color = coreColors.last().copy(alpha = waveAlpha * 0.4f),
                    radius = baseRadius * 1.22f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = coreColors.first().copy(alpha = waveAlpha * 0.7f),
                    radius = baseRadius * 1.10f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 3. Main Orb core sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isActive) {
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            coreColors[0],
                            coreColors[1],
                            coreColors.getOrElse(2) { coreColors[1] }
                        )
                    } else {
                        coreColors
                    },
                    center = Offset(center.x - baseRadius * 0.25f, center.y - baseRadius * 0.25f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 4. Orbital satellite nodes / sparks for Thinking/Speaking
            if (isActive) {
                val rad = Math.toRadians(rotationAngle.toDouble())
                val orbitRadius = baseRadius * 1.15f
                val dot1 = Offset(
                    center.x + (orbitRadius * cos(rad)).toFloat(),
                    center.y + (orbitRadius * sin(rad)).toFloat()
                )
                val dot2 = Offset(
                    center.x + (orbitRadius * cos(rad + Math.PI)).toFloat(),
                    center.y + (orbitRadius * sin(rad + Math.PI)).toFloat()
                )

                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = dot1
                )
                drawCircle(
                    color = coreColors.first(),
                    radius = 3.5.dp.toPx(),
                    center = dot2
                )
            }
        }
    }
}
