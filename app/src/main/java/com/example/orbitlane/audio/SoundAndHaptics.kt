package com.example.orbitlane.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class SoundAndHaptics(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playLaneSwitch(soundEnabled: Boolean, hapticsEnabled: Boolean, view: View? = null) {
        if (hapticsEnabled) {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                ?: triggerVibration(12L)
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (_: Exception) {}
        }
    }

    fun playDodge(soundEnabled: Boolean, hapticsEnabled: Boolean, view: View? = null) {
        if (hapticsEnabled) {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                ?: triggerVibration(18L)
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 40)
            } catch (_: Exception) {}
        }
    }

    fun playLapComplete(soundEnabled: Boolean, hapticsEnabled: Boolean, view: View? = null) {
        if (hapticsEnabled) {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                ?: triggerVibration(45L)
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 80)
            } catch (_: Exception) {}
        }
    }

    fun playCollision(soundEnabled: Boolean, hapticsEnabled: Boolean, view: View? = null) {
        if (hapticsEnabled) {
            triggerVibration(160L)
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 140)
            } catch (_: Exception) {}
        }
    }

    private fun triggerVibration(millis: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            millis,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(millis)
                }
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
