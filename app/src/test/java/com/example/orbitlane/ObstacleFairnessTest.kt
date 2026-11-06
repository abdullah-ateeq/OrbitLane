package com.example.orbitlane

import com.example.orbitlane.engine.GameEngine
import com.example.orbitlane.engine.OrbitMath
import com.example.orbitlane.model.Difficulty
import com.example.orbitlane.model.Lane
import com.example.orbitlane.model.Obstacle
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ObstacleFairnessTest {

    @Test
    fun testNeverBlocksAllThreeLanesSimultaneously() {
        val seededRandom = Random(12345)
        val engine = GameEngine(Difficulty.HARD, seededRandom)

        // Simulate 200 spawn iterations across various player angles
        for (i in 0 until 200) {
            val list = mutableListOf<Obstacle>()
            val playerAngle = (i * 0.15f) % OrbitMath.TWO_PI
            engine.spawnFairObstacles(playerAngle, list)

            if (list.isNotEmpty()) {
                // Group obstacles by similar angular window (within 0.25 rad)
                val blockedLanes = list.map { it.lane }.toSet()
                assertTrue(
                    "Simultaneous obstacles should never block all 3 lanes (found: ${blockedLanes.size})",
                    blockedLanes.size < Lane.TOTAL_LANES
                )
            }
        }
    }

    @Test
    fun testAlwaysLeavesAtLeastOneOpenLane() {
        val seededRandom = Random(999)
        val engine = GameEngine(Difficulty.MEDIUM, seededRandom)

        val list = mutableListOf<Obstacle>()
        for (i in 0 until 100) {
            engine.spawnFairObstacles(i * 0.2f, list)
        }

        // Verify that any group of obstacles spawned around the same angle has at least one free lane
        val groupedByAngle = list.groupBy { (it.angle * 10).toInt() }
        for ((_, group) in groupedByAngle) {
            val lanesBlocked = group.map { it.lane }.toSet()
            assertTrue(
                "Must always leave at least one open lane. Blocked: $lanesBlocked",
                lanesBlocked.size < 3
            )
        }
    }
}
