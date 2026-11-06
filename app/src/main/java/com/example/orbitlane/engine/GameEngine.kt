package com.example.orbitlane.engine

import com.example.orbitlane.model.Difficulty
import com.example.orbitlane.model.GameState
import com.example.orbitlane.model.Lane
import com.example.orbitlane.model.Obstacle
import com.example.orbitlane.model.ObstacleShape
import kotlin.math.max
import kotlin.random.Random

class GameEngine(
    initialDifficulty: Difficulty = Difficulty.MEDIUM,
    private val random: Random = Random.Default,
    var enableSpawning: Boolean = true
) {
    var state: GameState = GameState(difficulty = initialDifficulty)
        private set

    private var nextObstacleId: Long = 1L
    private var timeSinceLastSpawn: Float = 0f
    private var nextSpawnInterval: Float = 1.5f
    private var accumulatedPlayerAngle: Float = 0f
    private var lastSpawnedAngle: Float? = null
    private var lastBlockedLanes: Set<Int> = emptySet()

    init {
        reset(initialDifficulty)
    }

    fun reset(difficulty: Difficulty = state.difficulty) {
        state = GameState(
            playerLane = Lane.MIDDLE.index,
            visualLane = Lane.MIDDLE.index.toFloat(),
            playerAngle = 0f,
            obstacles = emptyList(),
            score = 0,
            streak = 0,
            bestStreak = 0,
            laps = 0,
            isGameOver = false,
            isPaused = false,
            dodgeFlashCounter = 0,
            lapFlashCounter = 0,
            shakeIntensity = 0f,
            collisionPoint = null,
            difficulty = difficulty
        )
        accumulatedPlayerAngle = 0f
        timeSinceLastSpawn = 0f
        nextSpawnInterval = calculateNextSpawnInterval()
        lastSpawnedAngle = null
        lastBlockedLanes = emptySet()
    }

    fun setPaused(paused: Boolean) {
        if (!state.isGameOver) {
            state = state.copy(isPaused = paused)
        }
    }

    fun switchInward(): Boolean {
        if (state.isGameOver || state.isPaused) return false
        val current = state.playerLane
        if (current > 0) {
            state = state.copy(playerLane = current - 1)
            return true
        }
        return false
    }

    fun switchOutward(): Boolean {
        if (state.isGameOver || state.isPaused) return false
        val current = state.playerLane
        if (current < Lane.TOTAL_LANES - 1) {
            state = state.copy(playerLane = current + 1)
            return true
        }
        return false
    }

    fun update(dt: Float) {
        if (state.isGameOver || state.isPaused) {
            // Decay screen shake
            if (state.shakeIntensity > 0f) {
                val decayed = (state.shakeIntensity - dt * 3.0f).coerceAtLeast(0f)
                state = state.copy(shakeIntensity = decayed)
            }
            return
        }

        val diff = state.difficulty
        // Subtle progression speed multiplier based on score
        val progressionMultiplier = 1.0f + ((state.score / 2500f) * 0.18f).coerceAtMost(0.35f)

        // 1. Smooth visual lane movement
        val laneSwitchRate = 14.0f
        val visualLaneDelta = (state.playerLane.toFloat() - state.visualLane) * (laneSwitchRate * dt).coerceAtMost(1f)
        val newVisualLane = state.visualLane + visualLaneDelta

        // 2. Advance player angle (counter-clockwise: increasing angle)
        val playerAngularDelta = diff.playerSpeed * progressionMultiplier * dt
        val newPlayerAngle = OrbitMath.normalizeAngle(state.playerAngle + playerAngularDelta)

        // 3. Lap detection
        accumulatedPlayerAngle += playerAngularDelta
        var newLaps = state.laps
        var newLapCounter = state.lapFlashCounter
        var lapBonusScore = 0
        while (accumulatedPlayerAngle >= OrbitMath.TWO_PI) {
            accumulatedPlayerAngle -= OrbitMath.TWO_PI
            newLaps++
            newLapCounter++
            lapBonusScore += 250
        }

        // 4. Advance obstacles and check collisions/dodges
        val obstacleAngularDelta = diff.obstacleSpeed * progressionMultiplier * dt
        var newScore = state.score + lapBonusScore
        var newStreak = state.streak
        var newBestStreak = state.bestStreak
        var newDodgeCounter = state.dodgeFlashCounter
        var collided = false
        var collisionPt: Pair<Float, Float>? = null

        val updatedObstacles = mutableListOf<Obstacle>()

        for (obs in state.obstacles) {
            val prevAngle = obs.angle
            // Obstacles move clockwise (decreasing angle towards player)
            val currAngle = OrbitMath.normalizeAngle(prevAngle - obstacleAngularDelta)
            obs.angle = currAngle

            if (!obs.passed) {
                // Collision check
                val isColliding = OrbitMath.checkCollision(
                    playerLaneFloat = newVisualLane,
                    playerAngle = newPlayerAngle,
                    obstacleLane = obs.lane,
                    obstacleAngle = currAngle
                )

                if (isColliding) {
                    collided = true
                    // Compute collision point on screen (standard radius fraction for collided lane)
                    val r = Lane.fromIndex(obs.lane).radiusFraction
                    val (cx, cy) = OrbitMath.polarToCartesian(0f, 0f, r, currAngle)
                    collisionPt = Pair(cx, cy)
                    break
                }

                // Check if safely passed player
                if (OrbitMath.hasObstaclePassedPlayer(prevAngle, currAngle, newPlayerAngle)) {
                    obs.passed = true
                    newStreak++
                    if (newStreak > newBestStreak) {
                        newBestStreak = newStreak
                    }
                    val currentMultiplier = when {
                        newStreak >= 15 -> 4
                        newStreak >= 8 -> 3
                        newStreak >= 4 -> 2
                        else -> 1
                    }
                    newScore += 100 * currentMultiplier
                    newDodgeCounter++
                }
            }

            // Keep obstacles that haven't moved too far past the player
            val distBehind = OrbitMath.signedAngularDifference(newPlayerAngle, currAngle)
            if (distBehind > -1.0f) {
                updatedObstacles.add(obs)
            }
        }

        if (collided) {
            state = state.copy(
                visualLane = newVisualLane,
                playerAngle = newPlayerAngle,
                isGameOver = true,
                shakeIntensity = 1.0f,
                collisionPoint = collisionPt,
                streak = 0
            )
            return
        }

        // 5. Spawn new obstacles
        if (enableSpawning) {
            timeSinceLastSpawn += dt
            if (timeSinceLastSpawn >= nextSpawnInterval) {
                timeSinceLastSpawn = 0f
                nextSpawnInterval = calculateNextSpawnInterval()
                spawnFairObstacles(newPlayerAngle, updatedObstacles)
            }
        }

        // 6. Shake decay
        val decayedShake = (state.shakeIntensity - dt * 2.5f).coerceAtLeast(0f)

        state = state.copy(
            visualLane = newVisualLane,
            playerAngle = newPlayerAngle,
            obstacles = updatedObstacles,
            score = newScore,
            streak = newStreak,
            bestStreak = newBestStreak,
            laps = newLaps,
            dodgeFlashCounter = newDodgeCounter,
            lapFlashCounter = newLapCounter,
            shakeIntensity = decayedShake
        )
    }

    private fun calculateNextSpawnInterval(): Float {
        val diff = state.difficulty
        val minSec = diff.spawnIntervalMinMs / 1000f
        val maxSec = diff.spawnIntervalMaxMs / 1000f
        return minSec + random.nextFloat() * (maxSec - minSec)
    }

    /**
     * Generates obstacles ahead of player while strictly guaranteeing fairness:
     * - Never blocks all 3 lanes at the same angular position.
     * - At most 2 lanes blocked simultaneously, leaving at least 1 lane completely free.
     * - Ensures sufficient angular lead time ahead of the player (between 2.4 and 3.2 radians ahead).
     * - Respects minimum angular spacing relative to previously spawned obstacles.
     */
    fun spawnFairObstacles(playerAngle: Float, list: MutableList<Obstacle>) {
        // Spawn ahead in the path of the player (obstacles are clockwise from player)
        val leadAngle = 2.4f + random.nextFloat() * 0.8f // ~140° to ~185° ahead
        val spawnAngle = OrbitMath.normalizeAngle(playerAngle + leadAngle)

        // Check angular spacing against previous spawn
        lastSpawnedAngle?.let { prev ->
            val separation = OrbitMath.angularDistance(spawnAngle, prev)
            if (separation < state.difficulty.minAngularSpacing) {
                return // Skip spawn this tick to maintain fair reaction spacing
            }
        }

        // Decide pattern: Single lane obstacle (~75%) or Two-lane gate (~25%)
        val allowDouble = state.difficulty != Difficulty.EASY && random.nextFloat() < 0.30f
        val shapes = ObstacleShape.entries

        if (allowDouble) {
            // Pick 1 lane to GUARANTEE safe passage
            val safeLane = random.nextInt(Lane.TOTAL_LANES)
            val blockedLanes = (0 until Lane.TOTAL_LANES).filter { it != safeLane }

            for (lane in blockedLanes) {
                list.add(
                    Obstacle(
                        id = nextObstacleId++,
                        lane = lane,
                        angle = spawnAngle,
                        speed = state.difficulty.obstacleSpeed,
                        shape = shapes[random.nextInt(shapes.size)]
                    )
                )
            }
            lastBlockedLanes = blockedLanes.toSet()
        } else {
            // Single obstacle: pick a lane that provides dynamic variety
            val lane = if (lastBlockedLanes.size == 1 && random.nextFloat() < 0.65f) {
                // Alternate lane for better rhythm
                val prev = lastBlockedLanes.first()
                val choices = (0 until Lane.TOTAL_LANES).filter { it != prev }
                choices[random.nextInt(choices.size)]
            } else {
                random.nextInt(Lane.TOTAL_LANES)
            }

            list.add(
                Obstacle(
                    id = nextObstacleId++,
                    lane = lane,
                    angle = spawnAngle,
                    speed = state.difficulty.obstacleSpeed,
                    shape = shapes[random.nextInt(shapes.size)]
                )
            )
            lastBlockedLanes = setOf(lane)
        }

        lastSpawnedAngle = spawnAngle
    }
}
