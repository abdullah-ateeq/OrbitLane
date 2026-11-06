package com.example.orbitlane.engine

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object OrbitMath {
    const val PI_FLOAT = PI.toFloat()
    const val TWO_PI = (2.0 * PI).toFloat()

    /**
     * Normalizes an angle in radians to the range [0, 2π).
     */
    fun normalizeAngle(angle: Float): Float {
        var a = angle % TWO_PI
        if (a < 0f) {
            a += TWO_PI
        }
        return a
    }

    /**
     * Returns the shortest absolute distance between two angles in radians [0, π].
     */
    fun angularDistance(a: Float, b: Float): Float {
        val diff = abs(normalizeAngle(a) - normalizeAngle(b))
        return if (diff > PI_FLOAT) TWO_PI - diff else diff
    }

    /**
     * Signed difference from angle [from] to angle [to] in the range [-π, π].
     * Positive means counter-clockwise direction (increasing angle).
     */
    fun signedAngularDifference(from: Float, to: Float): Float {
        val f = normalizeAngle(from)
        val t = normalizeAngle(to)
        var diff = t - f
        if (diff > PI_FLOAT) diff -= TWO_PI
        if (diff < -PI_FLOAT) diff += TWO_PI
        return diff
    }

    /**
     * Converts polar coordinates (radius, angle) with respect to a center (centerX, centerY)
     * to Cartesian (x, y) coordinates.
     */
    fun polarToCartesian(centerX: Float, centerY: Float, radius: Float, angle: Float): Pair<Float, Float> {
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        return Pair(x, y)
    }

    /**
     * Checks whether an obstacle and the player collide.
     * [playerLaneFloat] is the player's smoothed or logical lane.
     * [obstacleLane] is the obstacle's lane index (0, 1, or 2).
     * [laneTolerance] is maximum lane distance for collision (e.g., 0.40f).
     * [angleTolerance] is maximum angular distance for collision (e.g., obstacle angular size + player dot radius).
     */
    fun checkCollision(
        playerLaneFloat: Float,
        playerAngle: Float,
        obstacleLane: Int,
        obstacleAngle: Float,
        laneTolerance: Float = 0.45f,
        angleTolerance: Float = 0.22f
    ): Boolean {
        val laneDiff = abs(playerLaneFloat - obstacleLane.toFloat())
        if (laneDiff > laneTolerance) {
            return false
        }
        val angDist = angularDistance(playerAngle, obstacleAngle)
        return angDist <= angleTolerance
    }

    /**
     * Determines if an incoming obstacle (which is moving clockwise/counter-clockwise towards the player)
     * has just safely crossed the player's angular position.
     *
     * In OrbitLane:
     * Player moves counter-clockwise (increasing angle).
     * Obstacles move clockwise (decreasing angle towards player).
     * Therefore, before passing: obstacleAngle is ahead of player (obstacleAngle - playerAngle in [0, π]).
     * After passing: obstacleAngle is behind player (obstacleAngle - playerAngle in [-π, 0)).
     */
    fun hasObstaclePassedPlayer(
        prevAngle: Float,
        currAngle: Float,
        playerAngle: Float
    ): Boolean {
        val prevDiff = signedAngularDifference(playerAngle, prevAngle)
        val currDiff = signedAngularDifference(playerAngle, currAngle)
        // Was ahead of player (prevDiff > 0) and now behind or at player (currDiff <= 0)
        return prevDiff > 0f && currDiff <= 0f
    }
}
