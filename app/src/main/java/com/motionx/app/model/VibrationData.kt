package com.motionx.app.model

enum class VibrationStatus { NORMAL, VIBRATING, HIGH_VIBRATION }

/**
 * One physical reading. Raw axes include gravity and use m/s².
 * Magnitude, RMS, and peak use g after gravity suppression.
 * Timestamp is SensorEvent.timestamp: nanoseconds since boot, including deep sleep.
 * Filtering, window size, and thresholds must be documented when the analyzer is built.
 */
data class VibrationData(
    val accelerationX: Float,
    val accelerationY: Float,
    val accelerationZ: Float,
    val magnitude: Float,
    val rms: Float,
    val peak: Float,
    val status: VibrationStatus,
    val timestamp: Long,
)
