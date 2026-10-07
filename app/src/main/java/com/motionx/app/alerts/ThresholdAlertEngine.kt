package com.motionx.app.alerts

import com.motionx.app.model.AlertChannel
import com.motionx.app.model.MotionXUiState
import com.motionx.app.model.ThresholdAlertSettings

data class ThresholdAlert(val channels: Set<AlertChannel>, val sound: Boolean, val vibrate: Boolean)

/** Main-thread policy. Uses elapsed realtime for cooldown, producer timestamps only for freshness. */
class ThresholdAlertEngine {
    private class Gate {
        var settings: ThresholdAlertSettings? = null
        var timestamp: Long? = null
        var armed = true

        fun check(config: ThresholdAlertSettings, value: Float?, time: Long?, suppressed: Boolean): Boolean {
            if (settings != config) {
                settings = config
                timestamp = time // Editing settings must not alert on an old reading.
                armed = !suppressed
                return false
            }
            if (!config.enabled || value == null || !value.isFinite() || time == null) {
                timestamp = time
                // Missing data cannot rearm a latched alert; a valid low reading must do so.
                return false
            }
            val previous = timestamp
            if (previous == time) return false
            timestamp = time
            if (previous != null && time < previous) return false
            if (suppressed) {
                if (value >= config.threshold) armed = false
                return false
            }
            if (value < config.threshold * REARM_RATIO) armed = true
            if (armed && value >= config.threshold) {
                armed = false
                return true
            }
            return false
        }
    }

    private var visual = Gate()
    private var physical = Gate()
    private var session: Long? = null
    private var quietUntil = 0L

    fun reset() {
        visual = Gate()
        physical = Gate()
        session = null
        quietUntil = 0L
    }

    fun evaluate(state: MotionXUiState, nowMillis: Long): ThresholdAlert? {
        if (!state.isMonitoring) {
            reset()
            return null
        }
        if (session != state.monitoringSession) {
            reset()
            session = state.monitoringSession
        }
        val suppressed = nowMillis < quietUntil
        val channels = buildSet {
            if (state.visualAlert.isValid(AlertChannel.VISUAL) && visual.check(state.visualAlert,
                    state.visualMotion?.takeIf { it.isTracking }?.displacement,
                    state.visualMotion?.timestamp, suppressed)) add(AlertChannel.VISUAL)
            if (state.physicalAlert.isValid(AlertChannel.PHYSICAL) && physical.check(state.physicalAlert,
                    state.vibration?.magnitude, state.vibration?.timestamp, suppressed)) add(AlertChannel.PHYSICAL)
        }
        if (channels.isEmpty()) return null
        val settings = channels.map { if (it == AlertChannel.VISUAL) state.visualAlert else state.physicalAlert }
        val event = ThresholdAlert(channels, settings.any { it.sound }, settings.any { it.vibrate })
        quietUntil = nowMillis + COOLDOWN_MILLIS
        if (event.vibrate || event.sound) {
            // Speaker/haptic motion can reach either pipeline. Require fresh low readings
            // after the cooldown before either channel can generate another alert.
            visual.armed = false
            physical.armed = false
        }
        return event
    }

    companion object {
        const val COOLDOWN_MILLIS = 3_000L
        const val REARM_RATIO = 0.9f
    }
}
