package com.example.orbitlane.model

enum class ObstacleShape {
    DIAMOND,
    WEDGE,
    CRESCENT
}

data class Obstacle(
    val id: Long,
    val lane: Int,
    var angle: Float,
    val speed: Float,
    var passed: Boolean = false,
    val shape: ObstacleShape = ObstacleShape.DIAMOND,
    val angularSize: Float = 0.22f // In radians, collision bounding angle
)
