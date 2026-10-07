package com.motionx.app.sensors

import com.motionx.app.model.FrequencyData
import com.motionx.app.model.FrequencyStatus
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Bounded, timestamp-based spectrum of signed linear axes in g. Sensor-thread only. */
class FrequencyAnalyzer {
    private data class Sample(val time: Long, val axes: DoubleArray)
    private val samples = ArrayDeque<Sample>()
    private var lastAnalysis: Long? = null
    private var result = FrequencyData(FrequencyStatus.COLLECTING)

    fun reset() {
        samples.clear()
        lastAnalysis = null
        result = FrequencyData(FrequencyStatus.COLLECTING)
    }

    fun process(x: Float, y: Float, z: Float, time: Long): FrequencyData {
        if (!x.isFinite() || !y.isFinite() || !z.isFinite()) {
            reset()
            return result
        }
        samples.lastOrNull()?.let {
            if (time <= it.time || time - it.time > MAX_GAP_NANOS) reset()
        }
        samples.addLast(Sample(time, doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble())))
        val start = time - WINDOW_NANOS
        // Retain one sample before the window for interpolation, with a hard memory cap.
        while (samples.size > 2 && samples[1].time <= start) samples.removeFirst()
        while (samples.size > MAX_SAMPLES) samples.removeFirst()
        if (samples.first().time > start) {
            result = FrequencyData(FrequencyStatus.COLLECTING)
            return result
        }
        lastAnalysis?.let { if (time - it < UPDATE_NANOS) return result }
        lastAnalysis = time
        result = analyze(start)
        return result
    }

    private fun analyze(start: Long): FrequencyData {
        val span = (samples.last().time - samples.first().time) / 1e9
        val sampleRate = ((samples.size - 1) / span).toFloat()
        val largestGap = samples.zipWithNext { a, b -> b.time - a.time }.max() / 1e9
        // Conservative margin below Nyquist, based on the slowest interval in this window.
        val upper = minOf(MAX_HZ.toDouble(), 0.4 / largestGap).toFloat()
        fun unavailable(status: FrequencyStatus) = FrequencyData(status,
            sampleRateHz = sampleRate, upperLimitHz = upper)

        val axes = Array(3) { DoubleArray(POINTS) }
        var source = 0
        for (i in 0 until POINTS) {
            val target = start + i * WINDOW_NANOS / POINTS
            while (source + 1 < samples.lastIndex && samples[source + 1].time < target) source++
            val a = samples[source]
            val b = samples[source + 1]
            val fraction = (target - a.time).toDouble() / (b.time - a.time)
            for (axis in 0..2) axes[axis][i] = a.axes[axis] + fraction * (b.axes[axis] - a.axes[axis])
        }
        var energy = 0.0
        for (axis in axes) {
            val mean = axis.average()
            for (i in axis.indices) {
                val centered = axis[i] - mean
                energy += centered * centered
                axis[i] = centered * hann[i]
            }
        }
        if (sqrt(energy / POINTS) < MIN_RMS_G) return unavailable(FrequencyStatus.LOW_SIGNAL)

        // Sum axis powers instead of taking vector magnitude (which rectifies a sine).
        val powers = DoubleArray(POINTS / 2 + 1)
        for (bin in 1..POINTS / 2) {
            for (axis in axes) {
                var real = 0.0
                var imaginary = 0.0
                for (i in 0 until POINTS) {
                    real += axis[i] * cosine[bin - 1][i]
                    imaginary += axis[i] * sine[bin - 1][i]
                }
                powers[bin] += real * real + imaginary * imaginary
            }
        }
        val peak = (1..POINTS / 2).maxBy { powers[it] }
        val hz = peak / WINDOW_SECONDS
        val peakEnergy = ((peak - 1).coerceAtLeast(1)..(peak + 1).coerceAtMost(POINTS / 2))
            .sumOf { powers[it] }
        if (hz < MIN_HZ || hz > upper || peakEnergy / powers.sum() < MIN_PEAK_SHARE) {
            return unavailable(FrequencyStatus.NO_CLEAR_PEAK)
        }
        return FrequencyData(FrequencyStatus.READY, hz, sampleRate, upper)
    }

    companion object {
        const val WINDOW_SECONDS = 2f
        const val MIN_HZ = 2f
        const val MAX_HZ = 40f
        const val MIN_RMS_G = 0.005f
        private const val WINDOW_NANOS = 2_000_000_000L
        private const val UPDATE_NANOS = 500_000_000L
        private const val MAX_GAP_NANOS = 50_000_000L
        private const val MAX_SAMPLES = 1024
        private const val POINTS = 256
        private const val MIN_PEAK_SHARE = 0.6
        private val hann = DoubleArray(POINTS) { 0.5 - 0.5 * cos(2 * PI * it / (POINTS - 1)) }
        // Small fixed tables shared by sessions; avoid trigonometry on every sensor event.
        private val cosine = Array(POINTS / 2) { k -> DoubleArray(POINTS) { i -> cos(2 * PI * (k + 1) * i / POINTS) } }
        private val sine = Array(POINTS / 2) { k -> DoubleArray(POINTS) { i -> sin(2 * PI * (k + 1) * i / POINTS) } }
    }
}
