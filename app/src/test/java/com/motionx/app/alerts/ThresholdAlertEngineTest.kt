package com.motionx.app.alerts

import com.motionx.app.model.*
import org.junit.Assert.*
import org.junit.Test

class ThresholdAlertEngineTest {
    private val enabled = MotionXUiState(isMonitoring = true, monitoringSession = 1,
        visualAlert = ThresholdAlertSettings(10f, enabled = true),
        physicalAlert = ThresholdAlertSettings(0.1f, enabled = true))
    private fun sample(time: Long, visual: Float? = null, physical: Float? = null,
        base: MotionXUiState = enabled) = base.copy(
        visualMotion = visual?.let { VisualMotionData(0f, 0f, it, time, true) },
        vibration = physical?.let { VibrationData(0f, 0f, 9.8f, it, 0f, 0f, VibrationStatus.NORMAL, time) })
    private fun engine() = ThresholdAlertEngine().also { it.evaluate(enabled, 0) }

    @Test fun equalityTriggersBothChannelsAndCombinesFeedback() {
        val event = engine().evaluate(sample(1, 10f, 0.1f), 1)!!
        assertEquals(setOf(AlertChannel.VISUAL, AlertChannel.PHYSICAL), event.channels)
        assertTrue(event.sound)
        assertTrue(event.vibrate)
    }

    @Test fun sustainedHighDoesNotRepeatAndRequiresHysteresis() {
        val e = engine()
        assertNotNull(e.evaluate(sample(1, 11f), 1))
        assertNull(e.evaluate(sample(2, 12f), 4_000))
        assertNull(e.evaluate(sample(3, 9.5f), 4_100))
        assertNull(e.evaluate(sample(4, 10f), 4_200))
        assertNull(e.evaluate(sample(5, 8.9f), 4_300))
        assertNotNull(e.evaluate(sample(6, 10f), 4_400))
    }

    @Test fun feedbackCannotTriggerOtherChannelOrRearmDuringCooldown() {
        val e = engine()
        e.evaluate(sample(1, 11f, 0f), 1)
        assertNull(e.evaluate(sample(2, 0f, 0.3f), 200))
        assertNull(e.evaluate(sample(3, 0f, 0f), 500))
        assertNull(e.evaluate(sample(4, 11f, 0.3f), 3_500))
        assertNull(e.evaluate(sample(5, 0f, 0f), 3_600))
        assertEquals(setOf(AlertChannel.PHYSICAL), e.evaluate(sample(6, 0f, 0.2f), 3_700)!!.channels)
    }

    @Test fun missingTrackingOrSensorDataCannotRearmAnAlert() {
        val e = engine()
        e.evaluate(sample(1, 11f, 0.2f), 1)
        assertNull(e.evaluate(enabled, 4_000))
        assertNull(e.evaluate(sample(2, 11f, 0.2f), 4_100))
        val lost = sample(3, 0f).let { it.copy(visualMotion = it.visualMotion!!.copy(isTracking = false)) }
        assertNull(e.evaluate(lost, 4_200))
        assertNull(e.evaluate(sample(4, 11f), 4_300))
    }

    @Test fun unchangedSamplesCannotRearmOnUnrelatedStateUpdates() {
        val e = engine()
        e.evaluate(sample(1, 11f), 1)
        val low = sample(2, 0f)
        e.evaluate(low, 100)
        e.evaluate(low.copy(cameraReady = true), 4_000)
        assertNull(e.evaluate(sample(3, 11f), 4_100))
    }

    @Test fun settingsChangesSkipExistingReadingThenUseFreshSamples() {
        val e = engine()
        e.evaluate(sample(1, 8f), 1)
        val changed = sample(1, 8f).copy(visualAlert = enabled.visualAlert.copy(threshold = 5f))
        assertNull(e.evaluate(changed, 100))
        assertNotNull(e.evaluate(changed.copy(visualMotion = changed.visualMotion!!.copy(timestamp = 2)), 200))
    }

    @Test fun disabledInvalidAndUntrackedReadingsDoNotAlert() {
        val e = ThresholdAlertEngine()
        val disabled = enabled.copy(visualAlert = enabled.visualAlert.copy(enabled = false),
            physicalAlert = enabled.physicalAlert.copy(enabled = false))
        e.evaluate(disabled, 0)
        assertNull(e.evaluate(sample(1, 100f, 2f, disabled), 1))
        val normal = engine()
        assertNull(normal.evaluate(sample(1, Float.NaN, Float.POSITIVE_INFINITY), 1))
        assertNull(normal.evaluate(sample(2, 100f).let {
            it.copy(visualMotion = it.visualMotion!!.copy(isTracking = false)) }, 2))
    }

    @Test fun stopAndNewSessionClearLatchAndCooldown() {
        val e = engine()
        e.evaluate(sample(1, 11f), 1)
        assertNull(e.evaluate(enabled.copy(isMonitoring = false), 2))
        e.evaluate(enabled.copy(monitoringSession = 2), 3)
        assertNotNull(e.evaluate(sample(2, 11f).copy(monitoringSession = 2), 4))
    }

    @Test fun soundAndHapticFlagsAreIndependentWithVisualOnlyAlertsAllowed() {
        val e = ThresholdAlertEngine()
        val quiet = enabled.copy(visualAlert = enabled.visualAlert.copy(sound = false, vibrate = false))
        e.evaluate(quiet, 0)
        val event = e.evaluate(sample(1, 11f, base = quiet), 1)!!
        assertFalse(event.sound)
        assertFalse(event.vibrate)
        assertEquals(setOf(AlertChannel.VISUAL), event.channels)
    }

    @Test fun backwardsProducerTimeCannotProduceNewAlert() {
        val e = engine()
        e.evaluate(sample(100, 0f), 1)
        assertNull(e.evaluate(sample(50, 11f), 2))
        assertNotNull(e.evaluate(sample(60, 11f), 3))
    }
}
