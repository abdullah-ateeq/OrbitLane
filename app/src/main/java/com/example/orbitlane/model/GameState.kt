package com.example.orbitlane.model

data class GameState(
    val playerLane: Int = 1,                 // Current logical lane (0: Inner, 1: Middle, 2: Outer)
    val visualLane: Float = 1.0f,            // Smoothed floating-point position for animations
    val playerAngle: Float = 0.0f,           // 0 .. 2π
    val obstacles: List<Obstacle> = emptyList(),
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val laps: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val dodgeFlashCounter: Int = 0,          // Increments on each dodge to trigger HUD/canvas pulse
    val lapFlashCounter: Int = 0,            // Increments on each completed lap
    val shakeIntensity: Float = 0f,          // 0f .. 1f screen impact shake
    val collisionPoint: Pair<Float, Float>? = null, // (x, y) relative to arena center
    val difficulty: Difficulty = Difficulty.MEDIUM
) {
    val streakMultiplier: Int
        get() = when {
            streak >= 15 -> 4
            streak >= 8 -> 3
            streak >= 4 -> 2
            else -> 1
        }
}
