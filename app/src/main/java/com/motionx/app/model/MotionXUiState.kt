package com.motionx.app.model

/** Null readings mean unavailable, never a measured zero. */
data class MotionXUiState(
    val visualMotion: VisualMotionData? = null,
    val vibration: VibrationData? = null,
)
