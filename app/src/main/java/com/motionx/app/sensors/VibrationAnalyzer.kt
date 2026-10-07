package com.motionx.app.sensors

import com.motionx.app.model.VibrationData
import com.motionx.app.model.VibrationStatus
import kotlin.math.exp
import kotlin.math.sqrt

const val STANDARD_GRAVITY = 9.80665f

/**
 * Turns raw accelerometer samples (m/s², gravity included) into vibration metrics in g.
 *
 * Pipeline per sample:
 * 1. Gravity suppression: a first-order low-pass filter tracks gravity per axis;
 *    linear acceleration = raw - gravity.
 * 2. Magnitude: |linear| converted to g, then smoothed with an exponential moving average.
 * 3. RMS: root mean square of the unsmoothed linear magnitude over a sliding time window.
 * 4. Peak: maximum smoothed magnitude since construction or [reset], ignoring the warm-up.
 * 5. Status: RMS compared with [Config.vibratingThresholdG] and [Config.highThresholdG].
 *
 * Filters use real sample intervals from timestamps, so results do not depend on the
 * delivered sensor rate. Not thread-safe: feed it from one thread.
 */
class VibrationAnalyzer(private val config: Config = Config()) {

    /** Thresholds are uncalibrated defaults until tested on the target phone. */
    data class Config(
        val gravityTimeConstantSec: Float = 0.2f,
        val smoothingTimeConstantSec: Float = 0.05f,
        val rmsWindowSec: Float = 1.0f,
        val warmUpSec: Float = 0.6f,
        val maxSampleGapSec: Float = 0.5f,
        val vibratingThresholdG: Float = 0.03f,
        val highThresholdG: Float = 0.15f,
    )

    private val gravity = FloatArray(3)
    private val frequency = FrequencyAnalyzer()
    private var startNanos = 0L
    private var lastNanos = 0L
    private var hasSample = false
    private var smoothedG = 0f
    private var peakG = 0f

    private val rmsTimes = ArrayDeque<Long>()
    private val rmsSquares = ArrayDeque<Double>()
    private var rmsSum = 0.0

    fun reset() {
        frequency.reset()
        hasSample = false
        smoothedG = 0f
        peakG = 0f
        rmsTimes.clear()
        rmsSquares.clear()
        rmsSum = 0.0
    }

    fun process(x: Float, y: Float, z: Float, timestampNanos: Long): VibrationData {
        var gapSec = (timestampNanos - lastNanos) / 1e9f
        if (!hasSample || gapSec > config.maxSampleGapSec || gapSec < 0f) {
            // Seed gravity with the first sample (or after a long gap) so output starts near zero.
            reset()
            gravity[0] = x; gravity[1] = y; gravity[2] = z
            startNanos = timestampNanos
            hasSample = true
            gapSec = 0f
        } else {
            val alpha = lowPassAlpha(gapSec, config.gravityTimeConstantSec)
            gravity[0] += alpha * (x - gravity[0])
            gravity[1] += alpha * (y - gravity[1])
            gravity[2] += alpha * (z - gravity[2])
        }
        lastNanos = timestampNanos

        val lx = x - gravity[0]
        val ly = y - gravity[1]
        val lz = z - gravity[2]
        val instantG = sqrt(lx * lx + ly * ly + lz * lz) / STANDARD_GRAVITY

        smoothedG += lowPassAlpha(gapSec, config.smoothingTimeConstantSec) * (instantG - smoothedG)

        val rmsG = updateRms(instantG, timestampNanos)
        if (timestampNanos - startNanos >= (config.warmUpSec * 1e9f).toLong()) {
            peakG = maxOf(peakG, smoothedG)
        }

        return VibrationData(
            accelerationX = x,
            accelerationY = y,
            accelerationZ = z,
            magnitude = smoothedG,
            rms = rmsG,
            peak = peakG,
            status = statusFor(rmsG),
            timestamp = timestampNanos,
            frequency = frequency.process(lx / STANDARD_GRAVITY, ly / STANDARD_GRAVITY,
                lz / STANDARD_GRAVITY, timestampNanos),
        )
    }

    private fun updateRms(valueG: Float, timestampNanos: Long): Float {
        val square = valueG.toDouble() * valueG
        rmsTimes.addLast(timestampNanos)
        rmsSquares.addLast(square)
        rmsSum += square
        val windowStart = timestampNanos - (config.rmsWindowSec * 1e9f).toLong()
        while (rmsTimes.first() < windowStart) {
            rmsTimes.removeFirst()
            rmsSum -= rmsSquares.removeFirst()
        }
        return sqrt((rmsSum / rmsSquares.size).coerceAtLeast(0.0)).toFloat()
    }

    private fun statusFor(rmsG: Float): VibrationStatus = when {
        rmsG >= config.highThresholdG -> VibrationStatus.HIGH_VIBRATION
        rmsG >= config.vibratingThresholdG -> VibrationStatus.VIBRATING
        else -> VibrationStatus.NORMAL
    }

    /** Exact discrete-time coefficient for a first-order low-pass filter with time constant [tauSec]. */
    private fun lowPassAlpha(dtSec: Float, tauSec: Float): Float =
        if (dtSec <= 0f) 0f else 1f - exp(-dtSec / tauSec)
}
