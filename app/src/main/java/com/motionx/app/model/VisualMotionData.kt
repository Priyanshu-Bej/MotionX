package com.motionx.app.model

/**
 * Position in full, unrotated analysis-buffer pixels. Displacement is Euclidean
 * distance from the first valid marker centroid in the current tracking segment.
 * Losing tracking or restarting monitoring resets that reference. CameraX transforms
 * map buffer coordinates to the preview; these values are not display pixels.
 * Timestamp comes from CameraX ImageInfo.timestamp (nanoseconds); its clock must
 * be verified before aligning it with sensor events. Invalid tracking is explicit.
 */
data class VisualMotionData(
    val x: Float,
    val y: Float,
    val displacement: Float,
    val timestamp: Long,
    val isTracking: Boolean,
)
