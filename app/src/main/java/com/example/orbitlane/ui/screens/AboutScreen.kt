package com.example.orbitlane.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orbitlane.ui.theme.CyanAccent
import com.example.orbitlane.ui.theme.GraphiteBorder
import com.example.orbitlane.ui.theme.GraphiteCard
import com.example.orbitlane.ui.theme.TextMuted
import com.example.orbitlane.ui.theme.TextPrimary
import com.example.orbitlane.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GraphiteCard)
                        .border(1.dp, GraphiteBorder, CircleShape)
                ) {
                    Text("←", color = CyanAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "ABOUT",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            // Game Overview Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GraphiteCard)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "ORBITLANE",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version 1.0 (Arcade Edition)",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OrbitLane is a fast reflex arcade game where a glowing player dot travels continuously around concentric orbital paths. Dangerous hazards approach along the same circular tracks—switch lanes cleanly to avoid collisions and survive as long as possible.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gameplay Guide
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GraphiteCard)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                GuideItem(title = "INWARD LANE", desc = "Tap the top half of the arena or swipe up to shift inward.")
                Spacer(modifier = Modifier.height(12.dp))
                GuideItem(title = "OUTWARD LANE", desc = "Tap the bottom half or swipe down to shift outward.")
                Spacer(modifier = Modifier.height(12.dp))
                GuideItem(title = "STREAK MULTIPLIER", desc = "Dodge hazards in succession to activate score multipliers up to 4x.")
                Spacer(modifier = Modifier.height(12.dp))
                GuideItem(title = "ORBIT LAPS", desc = "Complete full 360° revolutions for bonus points and visual pulses.")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Architecture & Credits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GraphiteCard)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "SPECIFICATIONS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Built natively using Kotlin, Jetpack Compose, Material 3, and Compose Canvas. Completely offline, lightweight, and zero external game engine overhead.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun GuideItem(title: String, desc: String) {
    Column {
        Text(
            text = title,
            color = CyanAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
