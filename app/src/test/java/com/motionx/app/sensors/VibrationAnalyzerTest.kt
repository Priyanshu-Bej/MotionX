package com.motionx.app.sensors

import com.motionx.app.model.VibrationData
import com.motionx.app.model.VibrationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class VibrationAnalyzerTest {

    private val periodNanos = 10_000_000L // 100 Hz

    /** Feeds [seconds] of samples starting at [startNanos]; returns the last reading. */
    private fun VibrationAnalyzer.feed(
        seconds: Float,
        startNanos: Long = 0L,
        sample: (tSec: Double) -> FloatArray,
    ): VibrationData {
        val count = (seconds * 1e9f / periodNanos).toInt()
        var last: VibrationData? = null
        for (i in 0 until count) {
            val t = startNanos + i * periodNanos
            val (x, y, z) = sample(t / 1e9)
            last = process(x, y, z, t)
        }
        return last!!
    }

    private fun sineOnX(amplitudeG: Float, frequencyHz: Double) = { t: Double ->
        floatArrayOf(
            (amplitudeG * STANDARD_GRAVITY * sin(2 * PI * frequencyHz * t)).toFloat(),
            0f,
            STANDARD_GRAVITY,
        )
    }

    @Test
    fun stationaryPhoneReadsZeroVibration() {
        val result = VibrationAnalyzer().feed(2f) { floatArrayOf(0f, 0f, STANDARD_GRAVITY) }

        assertEquals(0f, result.magnitude, 1e-4f)
        assertEquals(0f, result.rms, 1e-4f)
        assertEquals(0f, result.peak, 1e-4f)
        assertEquals(VibrationStatus.NORMAL, result.status)
    }

    @Test
    fun rawAxesAndTimestampArePassedThrough() {
        val result = VibrationAnalyzer().process(1.5f, -2f, 9.7f, 123_456_789L)

        assertEquals(1.5f, result.accelerationX)
        assertEquals(-2f, result.accelerationY)
        assertEquals(9.7f, result.accelerationZ)
        assertEquals(123_456_789L, result.timestamp)
    }

    @Test
    fun tiltSettlesBackToNormalAfterGravitySuppression() {
        val analyzer = VibrationAnalyzer()
        analyzer.feed(1f) { floatArrayOf(0f, 0f, STANDARD_GRAVITY) }
        // Phone rotated so gravity now lies on X.
        val result = analyzer.feed(2f, startNanos = 1_000_000_000L) { floatArrayOf(STANDARD_GRAVITY, 0f, 0f) }

        assertEquals(0f, result.magnitude, 1e-3f)
        assertEquals(VibrationStatus.NORMAL, result.status)
    }

    @Test
    fun moderateVibrationMeasuresRmsAndIsVibrating() {
        val result = VibrationAnalyzer().feed(2f, sample = sineOnX(0.1f, 10.0))

        assertEquals(0.1f / sqrt(2f), result.rms, 0.005f)
        assertEquals(VibrationStatus.VIBRATING, result.status)
    }

    @Test
    fun strongVibrationIsHighWithPeakBetweenRmsAndAmplitude() {
        val result = VibrationAnalyzer().feed(2f, sample = sineOnX(0.5f, 10.0))

        assertEquals(0.5f / sqrt(2f), result.rms, 0.02f)
        assertEquals(VibrationStatus.HIGH_VIBRATION, result.status)
        assertTrue("peak ${result.peak}", result.peak > 0.25f && result.peak <= 0.5f)
    }

    @Test
    fun rmsDecaysAfterVibrationStops() {
        val analyzer = VibrationAnalyzer()
        analyzer.feed(2f, sample = sineOnX(0.5f, 10.0))
        val result = analyzer.feed(2f, startNanos = 2_000_000_000L) { floatArrayOf(0f, 0f, STANDARD_GRAVITY) }

        assertTrue("rms ${result.rms}", result.rms < 0.03f)
        assertEquals(VibrationStatus.NORMAL, result.status)
        assertTrue("peak is held", result.peak > 0.25f)
    }

    @Test
    fun resetClearsPeak() {
        val analyzer = VibrationAnalyzer()
        analyzer.feed(2f, sample = sineOnX(0.5f, 10.0))
        analyzer.reset()
        val result = analyzer.feed(2f, startNanos = 2_000_000_000L) { floatArrayOf(0f, 0f, STANDARD_GRAVITY) }

        assertEquals(0f, result.peak, 1e-4f)
    }

    @Test
    fun peakIgnoresWarmUp() {
        // Strong shake only during the first 0.3 s, inside the 0.6 s warm-up.
        val analyzer = VibrationAnalyzer()
        analyzer.feed(0.3f, sample = sineOnX(0.5f, 10.0))
        val result = analyzer.feed(2f, startNanos = 300_000_000L) { floatArrayOf(0f, 0f, STANDARD_GRAVITY) }

        assertTrue("peak ${result.peak}", result.peak < 0.05f)
    }
}
