package com.motionx.app.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.Vibrator

/** Short foreground feedback only; never changes system volume or bypasses silent mode. */
class AlertFeedback(context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val vibrator = context.getSystemService(Vibrator::class.java)
    private var tone: ToneGenerator? = null
    private val soundHandler = Handler(Looper.getMainLooper())

    fun play(alert: ThresholdAlert) {
        stop()
        if (alert.sound && audio?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Three 400 ms buzzer bursts separated by 120 ms; fits within the 3 s cooldown.
            playBuzzerBurst()
            for (delayMillis in listOf(520L, 1_040L)) {
                soundHandler.postDelayed({ playBuzzerBurst() }, delayMillis)
            }
        }
        if (alert.vibrate && audio?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
            runCatching {
                if (vibrator?.hasVibrator() == true) {
                    val effect = VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    if (Build.VERSION.SDK_INT >= 33) vibrator.vibrate(effect,
                        VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_NOTIFICATION).build())
                    else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    }
                }
            }
        }
    }

    private fun playBuzzerBurst() {
        // Recheck mode for each pulse in case the user mutes the phone mid-alert.
        if (audio?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        runCatching {
            val generator = tone ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70).also { tone = it }
            generator.startTone(ToneGenerator.TONE_PROP_NACK, 400)
        }
    }

    fun stop() {
        soundHandler.removeCallbacksAndMessages(null)
        runCatching { tone?.stopTone() }
        runCatching { vibrator?.cancel() }
    }

    fun release() {
        stop()
        runCatching { tone?.release() }
        tone = null
    }
}
