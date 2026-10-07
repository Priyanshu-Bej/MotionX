package com.motionx.app.model

enum class CameraProblem { UNAVAILABLE, OPEN_FAILED, ANALYSIS_FAILED }

/** Null readings mean unavailable, never a measured zero. */
data class MotionXUiState(
    val visualMotion: VisualMotionData? = null,
    val vibration: VibrationData? = null,
    val isMonitoring: Boolean = false,
    val cameraReady: Boolean = false,
    val cameraProblem: CameraProblem? = null,
)
