package com.example.orbitlane.model

enum class Lane(val index: Int, val displayName: String, val radiusFraction: Float) {
    INNER(0, "Inner", 0.46f),
    MIDDLE(1, "Middle", 0.70f),
    OUTER(2, "Outer", 0.94f);

    companion object {
        const val TOTAL_LANES = 3

        fun fromIndex(index: Int): Lane {
            return when {
                index <= 0 -> INNER
                index == 1 -> MIDDLE
                else -> OUTER
            }
        }

        fun clampIndex(index: Int): Int {
            return index.coerceIn(0, TOTAL_LANES - 1)
        }

        fun getRadiusForFraction(lanePosition: Float, arenaRadius: Float): Float {
            // Smooth linear interpolation across lane positions (0.0 .. 2.0)
            val clamped = lanePosition.coerceIn(0f, (TOTAL_LANES - 1).toFloat())
            val lower = clamped.toInt()
            val upper = (lower + 1).coerceAtMost(TOTAL_LANES - 1)
            val fraction = clamped - lower

            val rLower = fromIndex(lower).radiusFraction * arenaRadius
            val rUpper = fromIndex(upper).radiusFraction * arenaRadius
            return rLower + (rUpper - rLower) * fraction
        }
    }

    fun nextInward(): Lane = if (index > 0) fromIndex(index - 1) else this
    fun nextOutward(): Lane = if (index < TOTAL_LANES - 1) fromIndex(index + 1) else this
}
