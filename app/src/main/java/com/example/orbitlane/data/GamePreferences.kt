package com.example.orbitlane.data

import android.content.Context
import android.content.SharedPreferences
import com.example.orbitlane.model.Difficulty

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "orbit_lane_preferences"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_HIGHEST_STREAK = "key_highest_streak"
        private const val KEY_TOTAL_GAMES = "key_total_games"
        private const val KEY_DIFFICULTY = "key_difficulty"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_THEME = "key_theme" // "SYSTEM", "DARK", "LIGHT"
    }

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_BEST_SCORE, value).apply()

    var highestStreak: Int
        get() = prefs.getInt(KEY_HIGHEST_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_STREAK, value).apply()

    var totalGames: Int
        get() = prefs.getInt(KEY_TOTAL_GAMES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_GAMES, value).apply()

    var difficulty: Difficulty
        get() = Difficulty.fromName(prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name))
        set(value) = prefs.edit().putString(KEY_DIFFICULTY, value.name).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var themePreference: String
        get() = prefs.getString(KEY_THEME, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    /**
     * Updates statistics after a run. Returns true if a new high score was set.
     */
    fun recordGameFinished(finalScore: Int, maxStreak: Int): Boolean {
        var isNewBest = false
        val currentBest = bestScore
        if (finalScore > currentBest) {
            bestScore = finalScore
            isNewBest = true
        }
        if (maxStreak > highestStreak) {
            highestStreak = maxStreak
        }
        totalGames += 1
        return isNewBest
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_HIGHEST_STREAK, 0)
            .putInt(KEY_TOTAL_GAMES, 0)
            .apply()
    }
}
