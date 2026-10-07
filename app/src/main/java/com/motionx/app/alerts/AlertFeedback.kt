package com.motionx.app.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Build
import android.os.VibrationAttributes
import android.os.Vibrator

/** Short foreground feedback only; never changes system volume or bypasses silent mode. */
class AlertFeedback(context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val vibrator = context.getSystemService(Vibrator::class.java)
    private var tone: ToneGenerator? = null

    fun play(alert: ThresholdAlert) {
        if (alert.sound && audio?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            runCatching {
                val generator = tone ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70).also { tone = it }
                generator.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
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

    fun stop() {
        runCatching { tone?.stopTone() }
        runCatching { vibrator?.cancel() }
    }

    fun release() {
        stop()
        runCatching { tone?.release() }
        tone = null
    }
}
