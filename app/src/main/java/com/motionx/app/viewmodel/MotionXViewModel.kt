package com.motionx.app.viewmodel

import androidx.lifecycle.ViewModel
import com.motionx.app.model.MotionXUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MotionXViewModel : ViewModel() {
    // Connect real camera/sensor flows here as the pipelines are implemented.
    private val _uiState = MutableStateFlow(MotionXUiState())
    val uiState: StateFlow<MotionXUiState> = _uiState.asStateFlow()
}
