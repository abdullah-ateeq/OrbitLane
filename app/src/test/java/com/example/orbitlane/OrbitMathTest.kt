package com.example.orbitlane

import com.example.orbitlane.engine.OrbitMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class OrbitMathTest {
    private val epsilon = 0.0001f

    @Test
    fun testAngularWraparound() {
        // Normal angles remain within [0, 2π)
        val normal = 1.5f
        assertEquals(normal, OrbitMath.normalizeAngle(normal), epsilon)

        // Negative angle wraps around to positive equivalent
        val negative = -0.5f
        val expected = (2 * PI - 0.5).toFloat()
        assertEquals(expected, OrbitMath.normalizeAngle(negative), epsilon)

        // Angle exceeding 2π wraps around
        val excessive = (2 * PI + 0.75).toFloat()
        assertEquals(0.75f, OrbitMath.normalizeAngle(excessive), epsilon)

        // Exactly 2π wraps to 0
        assertEquals(0f, OrbitMath.normalizeAngle(OrbitMath.TWO_PI), epsilon)
    }

    @Test
    fun testAngularDistanceAcrossZeroBoundary() {
        // Two angles close together on either side of 0 / 2π
        val angleA = 0.05f
        val angleB = (2 * PI - 0.05).toFloat()

        val dist = OrbitMath.angularDistance(angleA, angleB)
        assertEquals(0.10f, dist, epsilon)
    }

    @Test
    fun testSignedAngularDifference() {
        val from = 0.1f
        val to = 0.4f
        assertEquals(0.3f, OrbitMath.signedAngularDifference(from, to), epsilon)

        // Crossing boundary
        val fromNearEnd = (2 * PI - 0.1).toFloat()
        val toNearStart = 0.2f
        val diff = OrbitMath.signedAngularDifference(fromNearEnd, toNearStart)
        assertEquals(0.3f, diff, epsilon)
    }

    @Test
    fun testPolarToCartesian() {
        val centerX = 100f
        val centerY = 100f
        val radius = 50f

        // Angle 0: along positive X
        val (x0, y0) = OrbitMath.polarToCartesian(centerX, centerY, radius, 0f)
        assertEquals(150f, x0, epsilon)
        assertEquals(100f, y0, epsilon)

        // Angle π/2: along positive Y
        val (x90, y90) = OrbitMath.polarToCartesian(centerX, centerY, radius, (PI / 2).toFloat())
        assertEquals(100f, x90, 0.01f)
        assertEquals(150f, y90, 0.01f)
    }

    @Test
    fun testCollisionDetection() {
        // Same lane and close angular position -> Collision
        val colliding = OrbitMath.checkCollision(
            playerLaneFloat = 1.0f,
            playerAngle = 1.0f,
            obstacleLane = 1,
            obstacleAngle = 1.05f
        )
        assertTrue(colliding)

        // Different lane -> No collision even if at identical angle
        val differentLane = OrbitMath.checkCollision(
            playerLaneFloat = 0.0f,
            playerAngle = 1.0f,
            obstacleLane = 2,
            obstacleAngle = 1.0f
        )
        assertFalse(differentLane)

        // Same lane but far angular distance -> No collision
        val farAngle = OrbitMath.checkCollision(
            playerLaneFloat = 1.0f,
            playerAngle = 0.5f,
            obstacleLane = 1,
            obstacleAngle = 2.5f
        )
        assertFalse(farAngle)
    }

    @Test
    fun testHasObstaclePassedPlayer() {
        // Obstacle approaching from ahead (0.4 rad ahead) moves to behind player (-0.1 rad behind)
        val playerAngle = 1.0f
        val prevObstacleAngle = 1.4f
        val currObstacleAngle = 0.9f

        val passed = OrbitMath.hasObstaclePassedPlayer(
            prevAngle = prevObstacleAngle,
            currAngle = currObstacleAngle,
            playerAngle = playerAngle
        )
        assertTrue(passed)

        // Still ahead of player -> not passed
        val notPassed = OrbitMath.hasObstaclePassedPlayer(
            prevAngle = 1.4f,
            currAngle = 1.2f,
            playerAngle = playerAngle
        )
        assertFalse(notPassed)
    }
}
