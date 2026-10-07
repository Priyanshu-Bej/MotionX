package com.motionx.app.sensors

import com.motionx.app.model.FrequencyData
import com.motionx.app.model.FrequencyStatus
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class FrequencyAnalyzerTest {
    private fun feed(
        analyzer: FrequencyAnalyzer = FrequencyAnalyzer(),
        rate: Int = 100,
        jitter: Boolean = false,
        signal: (Double) -> FloatArray,
    ): FrequencyData {
        var result: FrequencyData? = null
        for (i in 0..rate * 4) {
            val time = i * (1_000_000_000L / rate) + if (jitter && i % 2 == 1) 1_000_000L else 0L
            val xyz = signal(time / 1e9)
            result = analyzer.process(xyz[0], xyz[1], xyz[2], time)
        }
        return result!!
    }

    private fun tone(t: Double, hz: Double, amplitude: Double = 0.1) =
        (amplitude * sin(2 * PI * hz * t)).toFloat()

    @Test fun detectsSignedAxisToneWithoutDoublingFrequency() {
        for (hz in listOf(2.0, 7.3, 10.0, 25.0, 40.0)) {
            val result = feed { t -> floatArrayOf(tone(t, hz), 0f, 0f) }
            assertEquals("$hz Hz: $result", FrequencyStatus.READY, result.status)
            assertEquals(hz.toFloat(), result.hz!!, 0.26f)
        }
    }

    @Test fun combinesAxesWithoutCancellationAndRemovesDc() {
        val result = feed { t -> floatArrayOf(tone(t, 12.0) + 0.7f, -tone(t, 12.0), 1f) }
        assertEquals(12f, result.hz!!, 0.01f)
    }

    @Test fun followsActualRateAndTimestampJitter() {
        val slow = feed(rate = 50) { t -> floatArrayOf(0f, tone(t, 8.0), 0f) }
        assertEquals(8f, slow.hz!!, 0.01f)
        assertEquals(50f, slow.sampleRateHz!!, 0.01f)
        assertEquals(20f, slow.upperLimitHz!!, 0.01f)
        val jitter = feed(jitter = true) { t -> floatArrayOf(0f, 0f, tone(t, 13.0)) }
        assertEquals(13f, jitter.hz!!, 0.01f)
    }

    @Test fun stationaryAndTinySignalsHaveNoFrequency() {
        for (amplitude in listOf(0.0, 0.001)) {
            val result = feed { t -> floatArrayOf(tone(t, 10.0, amplitude), 0f, 0f) }
            assertEquals(FrequencyStatus.LOW_SIGNAL, result.status)
            assertNull(result.hz)
        }
    }

    @Test fun noiseAndCompetingTonesDoNotInventDominantFrequency() {
        val random = Random(18)
        val noise = feed { FloatArray(3) { random.nextFloat() * 0.2f - 0.1f } }
        assertEquals(FrequencyStatus.NO_CLEAR_PEAK, noise.status)
        val mixed = feed { t -> floatArrayOf(tone(t, 8.0), tone(t, 17.0), 0f) }
        assertEquals(FrequencyStatus.NO_CLEAR_PEAK, mixed.status)
        assertNull(mixed.hz)
    }

    @Test fun outOfRangeDominantToneIsNotReportedAsInRangeLeakage() {
        for (hz in listOf(0.5, 1.0, 45.0)) {
            val result = feed { t -> floatArrayOf(tone(t, hz), 0f, 0f) }
            assertEquals("$hz: $result", FrequencyStatus.NO_CLEAR_PEAK, result.status)
            assertNull(result.hz)
        }
    }

    @Test fun resetGapAndNonIncreasingTimestampsDiscardPreviousEstimate() {
        for (next in listOf(4_100_000_000L, 4_000_000_000L, 1L)) {
            val analyzer = FrequencyAnalyzer()
            assertNotNull(feed(analyzer) { t -> floatArrayOf(tone(t, 10.0), 0f, 0f) }.hz)
            val result = analyzer.process(0f, 0f, 0f, next)
            assertEquals(FrequencyStatus.COLLECTING, result.status)
            assertNull(result.hz)
        }
        val analyzer = FrequencyAnalyzer()
        feed(analyzer) { t -> floatArrayOf(tone(t, 10.0), 0f, 0f) }
        analyzer.reset()
        assertNull(analyzer.process(0f, 0f, 0f, 5_000_000_000L).hz)
    }

    @Test fun estimateClearsWhenMotionStops() {
        val analyzer = FrequencyAnalyzer()
        feed(analyzer) { t -> floatArrayOf(tone(t, 10.0), 0f, 0f) }
        var result: FrequencyData? = null
        for (i in 401..700) result = analyzer.process(0f, 0f, 0f, i * 10_000_000L)
        assertEquals(FrequencyStatus.LOW_SIGNAL, result!!.status)
        assertNull(result.hz)
    }

    @Test fun invalidInputClearsEstimate() {
        val analyzer = FrequencyAnalyzer()
        feed(analyzer) { t -> floatArrayOf(tone(t, 10.0), 0f, 0f) }
        assertNull(analyzer.process(Float.NaN, 0f, 0f, 4_010_000_000L).hz)
    }
}
