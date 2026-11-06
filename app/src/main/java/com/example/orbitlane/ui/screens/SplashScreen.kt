package com.example.orbitlane.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orbitlane.ui.theme.CyanAccent
import com.example.orbitlane.ui.theme.ElectricBlue
import com.example.orbitlane.ui.theme.TextMuted
import com.example.orbitlane.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.7f) }
    val alphaAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(700))
        delay(1300)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radii = listOf(size.width * 0.22f, size.width * 0.35f, size.width * 0.48f)

                    for ((idx, r) in radii.withIndex()) {
                        drawCircle(
                            color = CyanAccent.copy(alpha = 0.25f + idx * 0.15f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.8f)
                        )
                    }

                    // Orbiting satellite dots
                    val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
                    val dot1X = center.x + radii[1] * cos(rotRad)
                    val dot1Y = center.y + radii[1] * sin(rotRad)
                    drawCircle(color = CyanAccent, radius = 5f, center = Offset(dot1X, dot1Y))

                    val dot2X = center.x + radii[2] * cos(-rotRad * 1.3f)
                    val dot2Y = center.y + radii[2] * sin(-rotRad * 1.3f)
                    drawCircle(color = ElectricBlue, radius = 4f, center = Offset(dot2X, dot2Y))

                    // Center core
                    drawCircle(color = Color.White, radius = 7f, center = center)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ORBITLANE",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SWITCH LANES. STAY IN ORBIT.",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}
