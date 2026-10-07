package com.motionx.app.model

enum class CameraProblem { UNAVAILABLE, OPEN_FAILED, ANALYSIS_FAILED }
enum class SensorProblem { UNAVAILABLE, READ_FAILED }

/** Null readings mean unavailable, never a measured zero. */
data class MotionXUiState(
    val visualMotion: VisualMotionData? = null,
    val visualMotionHistory: List<VisualMotionData> = emptyList(),
    val vibration: VibrationData? = null,
    val isMonitoring: Boolean = false,
    val cameraReady: Boolean = false,
    val cameraProblem: CameraProblem? = null,
    val sensorProblem: SensorProblem? = null,
    val vibrationHistory: List<VibrationData> = emptyList(),
    val monitoringSession: Long = 0,
)
