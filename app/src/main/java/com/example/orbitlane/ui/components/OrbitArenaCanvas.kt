package com.example.orbitlane.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.orbitlane.engine.OrbitMath
import com.example.orbitlane.model.GameState
import com.example.orbitlane.model.Lane
import com.example.orbitlane.model.Obstacle
import com.example.orbitlane.model.ObstacleShape
import com.example.orbitlane.ui.theme.CyanAccent
import com.example.orbitlane.ui.theme.CyanGlow
import com.example.orbitlane.ui.theme.ElectricBlue
import com.example.orbitlane.ui.theme.GoldenAmber
import com.example.orbitlane.ui.theme.NeonCoral
import com.example.orbitlane.ui.theme.NeonCoralGlow
import com.example.orbitlane.ui.theme.OrbitRingActiveColor
import com.example.orbitlane.ui.theme.OrbitRingColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun OrbitArenaCanvas(
    state: GameState,
    onSwitchInward: () -> Unit,
    onSwitchOutward: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for dodge feedback
    val dodgeAnim = remember { Animatable(0f) }
    LaunchedEffect(state.dodgeFlashCounter) {
        if (state.dodgeFlashCounter > 0) {
            dodgeAnim.snapTo(1f)
            dodgeAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }
    }

    // Pulse animation for lap completion
    val lapAnim = remember { Animatable(0f) }
    LaunchedEffect(state.lapFlashCounter) {
        if (state.lapFlashCounter > 0) {
            lapAnim.snapTo(1f)
            lapAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }

    // Ambient radar sweep / subtle ring rotation
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    // Gesture tracking state for swipes
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var dragConsumed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val arenaCenterY = size.height / 2f
                    // Tap upper half -> switch inward, Tap lower half -> switch outward
                    if (tapOffset.y < arenaCenterY) {
                        onSwitchInward()
                    } else {
                        onSwitchOutward()
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragY = 0f
                        dragConsumed = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y
                        val threshold = 32f // px threshold for swipe
                        if (!dragConsumed) {
                            if (totalDragY < -threshold) {
                                // Swipe up -> inward
                                onSwitchInward()
                                dragConsumed = true
                            } else if (totalDragY > threshold) {
                                // Swipe down -> outward
                                onSwitchOutward()
                                dragConsumed = true
                            }
                        }
                    },
                    onDragEnd = {
                        totalDragY = 0f
                        dragConsumed = false
                    },
                    onDragCancel = {
                        totalDragY = 0f
                        dragConsumed = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) * 0.44f

            // Calculate screen shake translation
            val shakeOffsetX: Float
            val shakeOffsetY: Float
            if (state.shakeIntensity > 0f) {
                val magnitude = state.shakeIntensity * 24f
                val rand = Random(System.nanoTime())
                shakeOffsetX = (rand.nextFloat() * 2f - 1f) * magnitude
                shakeOffsetY = (rand.nextFloat() * 2f - 1f) * magnitude
            } else {
                shakeOffsetX = 0f
                shakeOffsetY = 0f
            }

            translate(left = shakeOffsetX, top = shakeOffsetY) {
                // 1. Draw subtle background radar grid & orbital rings
                drawOrbitBackground(center, maxRadius, ambientPulse, lapAnim.value)

                // 2. Draw the 3 orbital rings
                drawConcentricLanes(
                    center = center,
                    maxRadius = maxRadius,
                    activeLaneIndex = state.playerLane,
                    dodgePulse = dodgeAnim.value,
                    lapPulse = lapAnim.value
                )

                // 3. Draw obstacles
                drawObstacles(
                    center = center,
                    maxRadius = maxRadius,
                    obstacles = state.obstacles
                )

                // 4. Draw player glowing dot and motion trail
                drawPlayer(
                    center = center,
                    maxRadius = maxRadius,
                    visualLane = state.visualLane,
                    playerAngle = state.playerAngle,
                    ambientPulse = ambientPulse,
                    isGameOver = state.isGameOver
                )

                // 5. Draw Dodge / Lap Feedback Waves
                if (dodgeAnim.value > 0f) {
                    val dodgeRadius = maxRadius * (0.8f + (1f - dodgeAnim.value) * 0.35f)
                    drawCircle(
                        color = CyanAccent.copy(alpha = dodgeAnim.value * 0.45f),
                        radius = dodgeRadius,
                        center = center,
                        style = Stroke(width = 3f * dodgeAnim.value)
                    )
                }

                if (lapAnim.value > 0f) {
                    val lapRadius = maxRadius * (0.5f + (1f - lapAnim.value) * 0.7f)
                    drawCircle(
                        color = GoldenAmber.copy(alpha = lapAnim.value * 0.5f),
                        radius = lapRadius,
                        center = center,
                        style = Stroke(width = 4f * lapAnim.value)
                    )
                }

                // 6. Draw collision impact burst if game over
                if (state.isGameOver && state.collisionPoint != null) {
                    val (colX, colY) = state.collisionPoint
                    val impactCenter = center + Offset(colX * maxRadius, colY * maxRadius)
                    drawCollisionBurst(impactCenter, state.shakeIntensity)
                }
            }
        }
    }
}

private fun DrawScope.drawOrbitBackground(
    center: Offset,
    maxRadius: Float,
    ambientPulse: Float,
    lapPulse: Float
) {
    // Subtle circular radial glow in center
    val centerGlowRadius = maxRadius * 0.35f * ambientPulse
    val centerBrush = Brush.radialGradient(
        colors = listOf(
            if (lapPulse > 0.05f) GoldenAmber.copy(alpha = 0.25f * lapPulse) else ElectricBlue.copy(alpha = 0.08f),
            Color.Transparent
        ),
        center = center,
        radius = centerGlowRadius
    )
    drawCircle(
        brush = centerBrush,
        radius = centerGlowRadius,
        center = center
    )

    // Subtle crosshair / radar lines
    val crossColor = Color(0x1800E5FF)
    val crossExt = maxRadius * 1.05f
    drawLine(
        color = crossColor,
        start = Offset(center.x - crossExt, center.y),
        end = Offset(center.x + crossExt, center.y),
        strokeWidth = 1f
    )
    drawLine(
        color = crossColor,
        start = Offset(center.x, center.y - crossExt),
        end = Offset(center.x, center.y + crossExt),
        strokeWidth = 1f
    )

    // Arena boundary ring
    drawCircle(
        color = Color(0x15FFFFFF),
        radius = maxRadius * 1.04f,
        center = center,
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawConcentricLanes(
    center: Offset,
    maxRadius: Float,
    activeLaneIndex: Int,
    dodgePulse: Float,
    lapPulse: Float
) {
    for (lane in Lane.entries) {
        val r = lane.radiusFraction * maxRadius
        val isActive = lane.index == activeLaneIndex

        val baseRingColor = when {
            lapPulse > 0f -> GoldenAmber.copy(alpha = 0.2f + 0.3f * lapPulse)
            isActive -> OrbitRingActiveColor.copy(alpha = 0.45f + 0.25f * dodgePulse)
            else -> OrbitRingColor
        }
        val strokeWidth = if (isActive) 2.2f else 1.2f

        // Draw orbital circle
        drawCircle(
            color = baseRingColor,
            radius = r,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Subtle accent ticks on active ring
        if (isActive) {
            val tickCount = 12
            for (i in 0 until tickCount) {
                val tickAngle = (i * (2 * Math.PI) / tickCount).toFloat()
                val tickX1 = center.x + (r - 3f) * cos(tickAngle)
                val tickY1 = center.y + (r - 3f) * sin(tickAngle)
                val tickX2 = center.x + (r + 3f) * cos(tickAngle)
                val tickY2 = center.y + (r + 3f) * sin(tickAngle)
                drawLine(
                    color = CyanAccent.copy(alpha = 0.35f),
                    start = Offset(tickX1, tickY1),
                    end = Offset(tickX2, tickY2),
                    strokeWidth = 1f
                )
            }
        }
    }
}

private fun DrawScope.drawObstacles(
    center: Offset,
    maxRadius: Float,
    obstacles: List<Obstacle>
) {
    for (obs in obstacles) {
        val lane = Lane.fromIndex(obs.lane)
        val r = lane.radiusFraction * maxRadius
        val angle = obs.angle

        val (obsX, obsY) = OrbitMath.polarToCartesian(center.x, center.y, r, angle)
        val obsCenter = Offset(obsX, obsY)

        // Draw subtle motion trail (approaching clockwise, so trail is counter-clockwise behind it)
        val trailSteps = 4
        for (step in 1..trailSteps) {
            val trailAngle = angle + (step * 0.045f) // trailing behind obstacle
            val (tx, ty) = OrbitMath.polarToCartesian(center.x, center.y, r, trailAngle)
            val alpha = (1f - (step.toFloat() / trailSteps)) * 0.35f
            val trailRadius = (8f - step * 1.5f).coerceAtLeast(2f)
            drawCircle(
                color = NeonCoral.copy(alpha = alpha),
                radius = trailRadius,
                center = Offset(tx, ty)
            )
        }

        // Draw obstacle glow
        drawCircle(
            color = NeonCoralGlow,
            radius = 16f,
            center = obsCenter
        )

        // Obstacle body based on shape
        when (obs.shape) {
            ObstacleShape.DIAMOND -> {
                drawDiamondObstacle(obsCenter, obs.angle, NeonCoral)
            }
            ObstacleShape.WEDGE -> {
                drawWedgeObstacle(obsCenter, obs.angle, NeonCoral)
            }
            ObstacleShape.CRESCENT -> {
                drawDiamondObstacle(obsCenter, obs.angle, NeonCoral)
            }
        }
    }
}

private fun DrawScope.drawDiamondObstacle(center: Offset, angle: Float, color: Color) {
    val size = 9f
    // Rotate diamond aligned with orbital tangent
    val degrees = Math.toDegrees(angle.toDouble()).toFloat() + 90f
    rotate(degrees = degrees, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size * 1.25f)
            lineTo(center.x + size * 0.85f, center.y)
            lineTo(center.x, center.y + size * 1.25f)
            lineTo(center.x - size * 0.85f, center.y)
            close()
        }
        drawPath(path = path, color = color)
        // Center core
        drawCircle(color = Color.White, radius = 2.5f, center = center)
    }
}

private fun DrawScope.drawWedgeObstacle(center: Offset, angle: Float, color: Color) {
    val size = 9f
    val degrees = Math.toDegrees(angle.toDouble()).toFloat() + 90f
    rotate(degrees = degrees, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size * 1.2f)
            lineTo(center.x + size, center.y + size * 0.9f)
            lineTo(center.x, center.y + size * 0.3f)
            lineTo(center.x - size, center.y + size * 0.9f)
            close()
        }
        drawPath(path = path, color = color)
        drawCircle(color = Color.White, radius = 2.5f, center = center)
    }
}

private fun DrawScope.drawPlayer(
    center: Offset,
    maxRadius: Float,
    visualLane: Float,
    playerAngle: Float,
    ambientPulse: Float,
    isGameOver: Boolean
) {
    val r = Lane.getRadiusForFraction(visualLane, maxRadius)
    val (px, py) = OrbitMath.polarToCartesian(center.x, center.y, r, playerAngle)
    val playerCenter = Offset(px, py)

    if (isGameOver) {
        // Player extinguished / shattered
        drawCircle(color = NeonCoral.copy(alpha = 0.6f), radius = 6f, center = playerCenter)
        return
    }

    // 1. Draw glowing motion trail behind player
    // Player moves counter-clockwise (increasing angle), so trail is behind it (decreasing angle)
    val trailSteps = 5
    for (step in 1..trailSteps) {
        val trailAngle = playerAngle - (step * 0.055f)
        val (tx, ty) = OrbitMath.polarToCartesian(center.x, center.y, r, trailAngle)
        val alpha = (1f - (step.toFloat() / trailSteps)) * 0.45f
        val trailRadius = (8f - step * 1.2f).coerceAtLeast(2f)
        drawCircle(
            color = ElectricBlue.copy(alpha = alpha),
            radius = trailRadius,
            center = Offset(tx, ty)
        )
    }

    // 2. Outer luminous halo
    drawCircle(
        color = CyanGlow.copy(alpha = 0.35f * ambientPulse),
        radius = 20f * ambientPulse,
        center = playerCenter
    )

    // 3. Medium glow ring
    drawCircle(
        color = CyanAccent.copy(alpha = 0.65f),
        radius = 12f,
        center = playerCenter
    )

    // 4. Solid player core dot
    drawCircle(
        color = Color.White,
        radius = 5.5f,
        center = playerCenter
    )
}

private fun DrawScope.drawCollisionBurst(center: Offset, intensity: Float) {
    val burstRadius = 28f * (1f + intensity)
    drawCircle(
        color = NeonCoral.copy(alpha = 0.75f * intensity),
        radius = burstRadius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.9f * intensity),
        radius = 10f * intensity,
        center = center
    )

    // Radiating spikes
    val spikeCount = 8
    for (i in 0 until spikeCount) {
        val ang = (i * (2 * Math.PI) / spikeCount).toFloat()
        val len = burstRadius * 1.4f
        val ex = center.x + len * cos(ang)
        val ey = center.y + len * sin(ang)
        drawLine(
            color = NeonCoral.copy(alpha = 0.8f * intensity),
            start = center,
            end = Offset(ex, ey),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}
