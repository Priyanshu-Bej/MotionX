package com.motionx.app.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.Vibrator
import com.motionx.app.R

/** Short foreground feedback only; never changes system volume or bypasses silent mode. */
class AlertFeedback(context: Context) {
    private val resources = context.applicationContext.resources
    private val audio = context.getSystemService(AudioManager::class.java)
    private val vibrator = context.getSystemService(Vibrator::class.java)
    private var player: MediaPlayer? = null
    private val soundHandler = Handler(Looper.getMainLooper())

    fun play(alert: ThresholdAlert) {
        stop()
        if (alert.sound && audio?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            playAlertSound()
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

    private fun playAlertSound() {
        runCatching {
            val sound = MediaPlayer().also { player = it }
            sound.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            resources.openRawResourceFd(R.raw.alert).use { asset ->
                sound.setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
            }
            sound.isLooping = false
            sound.setVolume(0.7f, 0.7f)
            sound.setOnPreparedListener { prepared ->
                if (player === prepared) {
                    if (audio?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                        runCatching { prepared.start() }.onFailure { stopSound() }
                    } else stopSound()
                }
            }
            sound.setOnCompletionListener { completed ->
                if (player === completed) stopSound()
            }
            sound.setOnErrorListener { failed, _, _ ->
                if (player === failed) stopSound()
                true
            }
            // Includes preparation time: playback must end before the shared 3 s cooldown.
            soundHandler.postDelayed({ stopSound() }, 2_500L)
            sound.prepareAsync()
        }.onFailure { stopSound() }
    }

    private fun stopSound() {
        soundHandler.removeCallbacksAndMessages(null)
        val previous = player
        player = null // Late preparation/completion callbacks cannot restart canceled audio.
        runCatching { previous?.release() }
    }

    fun stop() {
        stopSound()
        runCatching { vibrator?.cancel() }
    }

    fun release() {
        stop()
    }
}
