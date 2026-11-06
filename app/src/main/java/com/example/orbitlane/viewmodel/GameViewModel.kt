package com.example.orbitlane.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.orbitlane.audio.SoundAndHaptics
import com.example.orbitlane.data.GamePreferences
import com.example.orbitlane.engine.GameEngine
import com.example.orbitlane.model.Difficulty
import com.example.orbitlane.model.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = GamePreferences(application.applicationContext)
    val soundAndHaptics = SoundAndHaptics(application.applicationContext)

    private val engine = GameEngine(initialDifficulty = prefs.difficulty)

    private val _gameState = MutableStateFlow(engine.state)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Persistent stats & settings exposed as StateFlow
    private val _bestScore = MutableStateFlow(prefs.bestScore)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _highestStreak = MutableStateFlow(prefs.highestStreak)
    val highestStreak: StateFlow<Int> = _highestStreak.asStateFlow()

    private val _difficulty = MutableStateFlow(prefs.difficulty)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.soundEnabled)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.hapticsEnabled)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _themePreference = MutableStateFlow(prefs.themePreference)
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _isNewBestScore = MutableStateFlow(false)
    val isNewBestScore: StateFlow<Boolean> = _isNewBestScore.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastRecordedDodgeCount = 0
    private var lastRecordedLapCount = 0

    init {
        refreshPreferencesState()
    }

    fun refreshPreferencesState() {
        _bestScore.value = prefs.bestScore
        _highestStreak.value = prefs.highestStreak
        _difficulty.value = prefs.difficulty
        _soundEnabled.value = prefs.soundEnabled
        _hapticsEnabled.value = prefs.hapticsEnabled
        _themePreference.value = prefs.themePreference
    }

    fun startGame(selectedDiff: Difficulty? = null) {
        val diffToUse = selectedDiff ?: _difficulty.value
        engine.reset(diffToUse)
        _gameState.value = engine.state
        _isNewBestScore.value = false
        lastRecordedDodgeCount = 0
        lastRecordedLapCount = 0

        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                engine.update(dt)
                val currentState = engine.state
                _gameState.value = currentState

                // Audio / Haptic feedback detection
                if (currentState.dodgeFlashCounter > lastRecordedDodgeCount) {
                    lastRecordedDodgeCount = currentState.dodgeFlashCounter
                    soundAndHaptics.playDodge(_soundEnabled.value, _hapticsEnabled.value)
                }

                if (currentState.lapFlashCounter > lastRecordedLapCount) {
                    lastRecordedLapCount = currentState.lapFlashCounter
                    soundAndHaptics.playLapComplete(_soundEnabled.value, _hapticsEnabled.value)
                }

                if (currentState.isGameOver) {
                    soundAndHaptics.playCollision(_soundEnabled.value, _hapticsEnabled.value)
                    val isNewBest = prefs.recordGameFinished(currentState.score, currentState.bestStreak)
                    _isNewBestScore.value = isNewBest
                    refreshPreferencesState()
                    break
                }

                delay(16) // ~60fps tick rate
            }
        }
    }

    fun switchInward() {
        val switched = engine.switchInward()
        if (switched) {
            _gameState.value = engine.state
            soundAndHaptics.playLaneSwitch(_soundEnabled.value, _hapticsEnabled.value)
        }
    }

    fun switchOutward() {
        val switched = engine.switchOutward()
        if (switched) {
            _gameState.value = engine.state
            soundAndHaptics.playLaneSwitch(_soundEnabled.value, _hapticsEnabled.value)
        }
    }

    fun togglePause() {
        val newPaused = !engine.state.isPaused
        engine.setPaused(newPaused)
        _gameState.value = engine.state
    }

    fun resumeGame() {
        engine.setPaused(false)
        _gameState.value = engine.state
    }

    fun setDifficulty(newDiff: Difficulty) {
        prefs.difficulty = newDiff
        _difficulty.value = newDiff
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.soundEnabled = enabled
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.hapticsEnabled = enabled
        _hapticsEnabled.value = enabled
    }

    fun setThemePreference(theme: String) {
        prefs.themePreference = theme
        _themePreference.value = theme
    }

    fun resetStatistics() {
        prefs.resetStatistics()
        refreshPreferencesState()
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        soundAndHaptics.release()
    }
}
