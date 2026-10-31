package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlarmSoundHelper(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var alarmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            Log.e("AlarmSoundHelper", "Could not initialize ToneGenerator", e)
        }
    }

    fun startEmergencyAlarm() {
        if (alarmJob?.isActive == true) return

        alarmJob = scope.launch {
            try {
                // Repeating pulsing alarm siren
                while (isActive) {
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 350)
                    } catch (e: Exception) {
                        Log.e("AlarmSoundHelper", "Error playing tone", e)
                    }

                    // Vibrate pattern
                    vibrator?.let { vib ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val effect = VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE)
                            vib.vibrate(effect)
                        } else {
                            @Suppress("DEPRECATION")
                            vib.vibrate(350)
                        }
                    }

                    delay(450)
                }
            } finally {
                toneGenerator?.stopTone()
            }
        }
    }

    fun stopAlarm() {
        alarmJob?.cancel()
        alarmJob = null
        try {
            toneGenerator?.stopTone()
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmSoundHelper", "Error stopping alarm", e)
        }
    }

    fun release() {
        stopAlarm()
        toneGenerator?.release()
        toneGenerator = null
    }
}
