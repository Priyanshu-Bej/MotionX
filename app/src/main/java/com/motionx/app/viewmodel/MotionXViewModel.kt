package com.motionx.app.viewmodel

import androidx.lifecycle.ViewModel
import com.motionx.app.model.AlertChannel
import com.motionx.app.model.ThresholdAlertSettings
import com.motionx.app.model.MotionXUiState
import com.motionx.app.model.CameraProblem
import com.motionx.app.model.VisualMotionData
import com.motionx.app.model.VibrationData
import com.motionx.app.model.SensorProblem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MotionXViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MotionXUiState())
    val uiState: StateFlow<MotionXUiState> = _uiState.asStateFlow()

    fun setAlert(channel: AlertChannel, settings: ThresholdAlertSettings) {
        if (!settings.isValid(channel)) return
        _uiState.update {
            when (channel) {
                AlertChannel.VISUAL -> it.copy(visualAlert = settings)
                AlertChannel.PHYSICAL -> it.copy(physicalAlert = settings)
            }
        }
    }

    fun setCameraReady(ready: Boolean) {
        _uiState.update { it.copy(cameraReady = ready) }
    }

    fun cameraFailed(problem: CameraProblem) {
        _uiState.update { it.copy(cameraProblem = problem, cameraReady = false,
            isMonitoring = false, visualMotion = null, visualMotionHistory = emptyList(),
            vibration = null, vibrationHistory = emptyList()) }
    }

    fun retryCamera() {
        _uiState.update { it.copy(cameraProblem = null, cameraReady = false) }
    }

    fun toggleMonitoring() {
        _uiState.update {
            if (it.isMonitoring) it.copy(isMonitoring = false, visualMotion = null,
                visualMotionHistory = emptyList(), vibration = null, vibrationHistory = emptyList())
            else if (it.cameraReady && it.cameraProblem == null)
                it.copy(isMonitoring = true, visualMotion = null, vibration = null,
                    visualMotionHistory = emptyList(), vibrationHistory = emptyList(), sensorProblem = null,
                    monitoringSession = it.monitoringSession + 1)
            else it
        }
    }

    fun stopMonitoring() {
        _uiState.update { it.copy(isMonitoring = false, visualMotion = null,
            visualMotionHistory = emptyList(), vibration = null, vibrationHistory = emptyList()) }
    }

    fun onVisualMotion(data: VisualMotionData) {
        _uiState.update {
            if (!it.isMonitoring) return@update it
            val previous = it.visualMotion
            if (previous?.timestamp == data.timestamp) return@update it
            val history = if (previous != null && data.timestamp < previous.timestamp) emptyList()
                else it.visualMotionHistory.filter { sample -> data.timestamp - sample.timestamp <= 10_000_000_000L }
            // Keep invalid samples as gaps, never plot their zero placeholder as real motion.
            it.copy(visualMotion = data, visualMotionHistory = (history + data).takeLast(150))
        }
    }

    fun onVibration(data: VibrationData, session: Long) {
        _uiState.update {
            if (!it.isMonitoring || it.monitoringSession != session) return@update it
            // Analyze every sensor event, but publish at most 10 UI readings per second.
            val previous = it.vibration
            if (previous != null && data.timestamp - previous.timestamp in 0 until 100_000_000L)
                return@update it
            val history = if (previous != null && data.timestamp < previous.timestamp) emptyList()
                else it.vibrationHistory.filter { sample -> data.timestamp - sample.timestamp <= 10_000_000_000L }
            it.copy(vibration = data, sensorProblem = null,
                vibrationHistory = (history + data).takeLast(100))
        }
    }

    fun sensorFailed(problem: SensorProblem, session: Long) {
        _uiState.update {
            if (it.isMonitoring && it.monitoringSession == session)
                it.copy(sensorProblem = problem, vibration = null, vibrationHistory = emptyList())
            else it
        }
    }
}
