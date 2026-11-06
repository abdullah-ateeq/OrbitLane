package com.example.orbitlane.model

enum class Difficulty(
    val title: String,
    val description: String,
    val playerSpeed: Float,       // Rad/s (approx radians per second)
    val obstacleSpeed: Float,     // Rad/s closing speed
    val spawnIntervalMinMs: Long,
    val spawnIntervalMaxMs: Long,
    val minAngularSpacing: Float  // Minimum radians between successive obstacle clusters
) {
    EASY(
        title = "Easy",
        description = "Relaxed orbit speed, generous spacing",
        playerSpeed = 1.15f,
        obstacleSpeed = 1.25f,
        spawnIntervalMinMs = 1800L,
        spawnIntervalMaxMs = 2600L,
        minAngularSpacing = 1.35f
    ),
    MEDIUM(
        title = "Medium",
        description = "Balanced orbital pace, active dodging",
        playerSpeed = 1.55f,
        obstacleSpeed = 1.75f,
        spawnIntervalMinMs = 1200L,
        spawnIntervalMaxMs = 1900L,
        minAngularSpacing = 1.05f
    ),
    HARD(
        title = "Hard",
        description = "High velocity, rapid spawns, tight reflex windows",
        playerSpeed = 1.95f,
        obstacleSpeed = 2.25f,
        spawnIntervalMinMs = 850L,
        spawnIntervalMaxMs = 1400L,
        minAngularSpacing = 0.85f
    );

    companion object {
        fun fromName(name: String?): Difficulty {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
        }
    }
}
