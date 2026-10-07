package com.motionx.app.model

/**
 * Position and displacement in analysis-frame pixels. The tracker will define the
 * displacement reference and preview coordinate transform before integration.
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
