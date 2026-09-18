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
        !isActive -> listOf(Color(0xFF374151), Color(0xFF1F2937), Color(0xFF111827))
        orbState == OrbState.ERROR -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF7F1D1D))
        orbState == OrbState.LISTENING -> listOf(Color(0xFF00F0FF), Color(0xFF0284C7), Color(0xFF8B5CF6))
        orbState == OrbState.SPEAKING -> listOf(Color(0xFFD946EF), Color(0xFF8B5CF6), Color(0xFF06B6D4))
        orbState == OrbState.THINKING -> listOf(Color(0xFF6366F1), Color(0xFF00F5FF), Color(0xFF3B82F6))
        orbState == OrbState.PROCESSING -> listOf(Color(0xFF10B981), Color(0xFF00E5FF), Color(0xFF3B82F6))
        else -> listOf(Color(0xFF00F0FF), Color(0xFF6366F1), Color(0xFFA855F7))
    }

    Box(
        modifier = modifier.size(orbSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.68f * pulseScale

            // 1. Deep Atmospheric Outer Glow
            if (isActive) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColors.first().copy(alpha = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) 0.38f else 0.20f),
                            coreColors[1].copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.65f
                    ),
                    radius = baseRadius * 1.65f,
                    center = center
                )

                // 2. Futuristic Circular HUD Tech Rings
                val ringAlpha = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) 0.75f else 0.35f
                
                // Outer dotted/segmented radar arc
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(coreColors[0].copy(alpha = ringAlpha), coreColors[1].copy(alpha = 0.1f), coreColors[0].copy(alpha = ringAlpha)),
                        center = center
                    ),
                    startAngle = rotationAngle,
                    sweepAngle = 220f,
                    useCenter = false,
                    style = Stroke(width = 2.5.dp.toPx()),
                    topLeft = Offset(center.x - baseRadius * 1.36f, center.y - baseRadius * 1.36f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.72f, baseRadius * 2.72f)
                )

                // Counter-rotating tech ring
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(coreColors[1].copy(alpha = ringAlpha * 0.8f), Color.Transparent, coreColors.first().copy(alpha = ringAlpha * 0.8f)),
                        center = center
                    ),
                    startAngle = -rotationAngle * 1.5f,
                    sweepAngle = 160f,
                    useCenter = false,
                    style = Stroke(width = 1.8.dp.toPx()),
                    topLeft = Offset(center.x - baseRadius * 1.20f, center.y - baseRadius * 1.20f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.40f, baseRadius * 2.40f)
                )

                // Inner precision resonance ring
                drawCircle(
                    color = coreColors[0].copy(alpha = ringAlpha * 0.6f),
                    radius = baseRadius * 1.08f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 3. Main Orb Core Sphere with Multi-Stop Glowing Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isActive) {
                        listOf(
                            Color.White.copy(alpha = 0.98f),
                            coreColors[0].copy(alpha = 0.95f),
                            coreColors[1].copy(alpha = 0.85f),
                            coreColors.getOrElse(2) { coreColors[1] }.copy(alpha = 0.9f),
                            Color(0xFF0B0820)
                        )
                    } else {
                        coreColors
                    },
                    center = Offset(center.x - baseRadius * 0.22f, center.y - baseRadius * 0.22f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 4. Orbital Tech Satellite Nodes
            if (isActive) {
                val rad = Math.toRadians(rotationAngle.toDouble())
                val orbitRadius = baseRadius * 1.28f
                val dot1 = Offset(
                    center.x + (orbitRadius * cos(rad)).toFloat(),
                    center.y + (orbitRadius * sin(rad)).toFloat()
                )
                val dot2 = Offset(
                    center.x + (orbitRadius * cos(rad + Math.PI)).toFloat(),
                    center.y + (orbitRadius * sin(rad + Math.PI)).toFloat()
                )
                val dot3 = Offset(
                    center.x + (orbitRadius * 0.92f * cos(-rad * 1.3 + Math.PI / 2)).toFloat(),
                    center.y + (orbitRadius * 0.92f * sin(-rad * 1.3 + Math.PI / 2)).toFloat()
                )

                // Satellite 1
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = dot1
                )
                drawCircle(
                    color = coreColors[0].copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = dot1
                )

                // Satellite 2
                drawCircle(
                    color = coreColors.first(),
                    radius = 3.5.dp.toPx(),
                    center = dot2
                )

                // Satellite 3
                drawCircle(
                    color = coreColors[1],
                    radius = 3.dp.toPx(),
                    center = dot3
                )
            }
        }
    }
}
