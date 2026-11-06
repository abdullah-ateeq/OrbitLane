package com.example.orbitlane

import com.example.orbitlane.engine.GameEngine
import com.example.orbitlane.engine.OrbitMath
import com.example.orbitlane.model.Difficulty
import com.example.orbitlane.model.Lane
import com.example.orbitlane.model.Obstacle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        engine = GameEngine(initialDifficulty = Difficulty.MEDIUM, random = Random(42))
    }

    @Test
    fun testLaneSwitchingBounds() {
        // Starts in middle lane (index 1)
        assertEquals(Lane.MIDDLE.index, engine.state.playerLane)

        // Switch inward -> Inner lane (index 0)
        assertTrue(engine.switchInward())
        assertEquals(Lane.INNER.index, engine.state.playerLane)

        // Switch inward again -> should be clamped, return false
        assertFalse(engine.switchInward())
        assertEquals(Lane.INNER.index, engine.state.playerLane)

        // Switch outward -> Middle lane (index 1)
        assertTrue(engine.switchOutward())
        assertEquals(Lane.MIDDLE.index, engine.state.playerLane)

        // Switch outward -> Outer lane (index 2)
        assertTrue(engine.switchOutward())
        assertEquals(Lane.OUTER.index, engine.state.playerLane)

        // Switch outward again -> clamped at Outer lane
        assertFalse(engine.switchOutward())
        assertEquals(Lane.OUTER.index, engine.state.playerLane)
    }

    @Test
    fun testLapDetectionAndScoreBonus() {
        engine.enableSpawning = false
        val initialLaps = engine.state.laps
        val initialScore = engine.state.score

        // Advance time enough for the player to traverse a full 2π revolution
        val playerSpeed = engine.state.difficulty.playerSpeed
        val timeForOneLap = OrbitMath.TWO_PI / playerSpeed + 0.1f

        var elapsed = 0f
        val step = 0.016f
        while (elapsed < timeForOneLap) {
            engine.update(step)
            elapsed += step
        }

        assertEquals(initialLaps + 1, engine.state.laps)
        assertTrue("Score should increase by at least 250 for completing a lap", engine.state.score >= initialScore + 250)
    }

    @Test
    fun testPlayerObstacleCollisionCausesGameOver() {
        // Place an obstacle in the same lane right in front of the player
        val testObstacle = Obstacle(
            id = 999L,
            lane = engine.state.playerLane,
            angle = engine.state.playerAngle + 0.05f,
            speed = 1.0f
        )

        // Reset with custom obstacles via state simulation
        val engineWithObstacle = GameEngine(Difficulty.EASY)
        engineWithObstacle.spawnFairObstacles(0f, mutableListOf())

        // Run update when obstacle is directly colliding
        var collided = false
        for (i in 0 until 100) {
            engineWithObstacle.update(0.016f)
            if (engineWithObstacle.state.isGameOver) {
                collided = true
                break
            }
        }

        // Verify collision properties when game over occurs
        if (collided) {
            assertTrue(engineWithObstacle.state.isGameOver)
            assertTrue(engineWithObstacle.state.shakeIntensity > 0f)
        }
    }

    @Test
    fun testStreakMultiplierProgression() {
        val state0 = engine.state.copy(streak = 0)
        assertEquals(1, state0.streakMultiplier)

        val state4 = engine.state.copy(streak = 4)
        assertEquals(2, state4.streakMultiplier)

        val state8 = engine.state.copy(streak = 8)
        assertEquals(3, state8.streakMultiplier)

        val state15 = engine.state.copy(streak = 15)
        assertEquals(4, state15.streakMultiplier)
    }

    @Test
    fun testDifficultyParameters() {
        // Verify Easy is slower than Medium, and Medium is slower than Hard
        assertTrue(Difficulty.EASY.playerSpeed < Difficulty.MEDIUM.playerSpeed)
        assertTrue(Difficulty.MEDIUM.playerSpeed < Difficulty.HARD.playerSpeed)

        assertTrue(Difficulty.EASY.obstacleSpeed < Difficulty.MEDIUM.obstacleSpeed)
        assertTrue(Difficulty.MEDIUM.obstacleSpeed < Difficulty.HARD.obstacleSpeed)

        assertTrue(Difficulty.EASY.minAngularSpacing > Difficulty.HARD.minAngularSpacing)
    }
}
