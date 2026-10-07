package com.motionx.app.viewmodel

import androidx.lifecycle.ViewModel
import com.motionx.app.model.MotionXUiState
import com.motionx.app.model.CameraProblem
import com.motionx.app.model.VisualMotionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MotionXViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MotionXUiState())
    val uiState: StateFlow<MotionXUiState> = _uiState.asStateFlow()

    fun setCameraReady(ready: Boolean) {
        _uiState.update { it.copy(cameraReady = ready) }
    }

    fun cameraFailed(problem: CameraProblem) {
        _uiState.update { it.copy(cameraProblem = problem, cameraReady = false,
            isMonitoring = false, visualMotion = null) }
    }

    fun retryCamera() {
        _uiState.update { it.copy(cameraProblem = null, cameraReady = false) }
    }

    fun toggleMonitoring() {
        _uiState.update {
            if (it.isMonitoring) it.copy(isMonitoring = false, visualMotion = null)
            else if (it.cameraReady && it.cameraProblem == null)
                it.copy(isMonitoring = true, visualMotion = null)
            else it
        }
    }

    fun stopMonitoring() {
        _uiState.update { it.copy(isMonitoring = false, visualMotion = null) }
    }

    fun onVisualMotion(data: VisualMotionData) {
        _uiState.update { if (it.isMonitoring) it.copy(visualMotion = data) else it }
    }
}
