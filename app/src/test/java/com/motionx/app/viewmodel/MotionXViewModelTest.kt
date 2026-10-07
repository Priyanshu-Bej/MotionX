package com.motionx.app.viewmodel

import com.motionx.app.model.CameraProblem
import com.motionx.app.model.VisualMotionData
import org.junit.Assert.*
import org.junit.Test

class MotionXViewModelTest {
    private val reading = VisualMotionData(10f, 20f, 4f, 100, true)

    @Test fun cannotStartWithoutReadyCamera() {
        val model = MotionXViewModel()
        model.toggleMonitoring()
        assertFalse(model.uiState.value.isMonitoring)
    }

    @Test fun stopClearsReadingsAndRejectsLateFrames() {
        val model = MotionXViewModel()
        model.setCameraReady(true)
        model.toggleMonitoring()
        model.onVisualMotion(reading)
        assertEquals(reading, model.uiState.value.visualMotion)
        model.stopMonitoring()
        model.onVisualMotion(reading)
        assertFalse(model.uiState.value.isMonitoring)
        assertNull(model.uiState.value.visualMotion)
        assertNull(model.uiState.value.vibration)
    }

    @Test fun cameraFailureStopsCollectionAndRetryClearsError() {
        val model = MotionXViewModel()
        model.setCameraReady(true)
        model.toggleMonitoring()
        model.onVisualMotion(reading)
        model.cameraFailed(CameraProblem.OPEN_FAILED)
        assertFalse(model.uiState.value.isMonitoring)
        assertFalse(model.uiState.value.cameraReady)
        assertNull(model.uiState.value.visualMotion)
        model.retryCamera()
        assertNull(model.uiState.value.cameraProblem)
    }
}
