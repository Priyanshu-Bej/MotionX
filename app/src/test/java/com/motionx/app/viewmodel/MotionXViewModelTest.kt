package com.motionx.app.viewmodel

import com.motionx.app.model.CameraProblem
import com.motionx.app.model.VisualMotionData
import com.motionx.app.model.VibrationData
import com.motionx.app.model.VibrationStatus
import com.motionx.app.model.SensorProblem
import org.junit.Assert.*
import org.junit.Test

class MotionXViewModelTest {
    private val reading = VisualMotionData(10f, 20f, 4f, 100, true)
    private fun vibration(time: Long) = VibrationData(0f, 0f, 9.8f, 0.04f,
        0.05f, 0.1f, VibrationStatus.VIBRATING, time)

    @Test fun sensorUpdatesPreserveCameraAndHistoryStaysBounded() {
        val model = MotionXViewModel()
        model.setCameraReady(true)
        model.toggleMonitoring()
        val session = model.uiState.value.monitoringSession
        model.onVisualMotion(reading)
        repeat(150) { model.onVibration(vibration(it * 100_000_000L), session) }
        assertEquals(reading, model.uiState.value.visualMotion)
        assertEquals(100, model.uiState.value.vibrationHistory.size)
        assertEquals(vibration(14_900_000_000L), model.uiState.value.vibration)
        model.onVibration(vibration(14_910_000_000L), session)
        assertEquals(vibration(14_900_000_000L), model.uiState.value.vibration)
    }

    @Test fun stoppingAndRestartingRejectsOldSensorSessions() {
        val model = MotionXViewModel()
        model.setCameraReady(true)
        model.toggleMonitoring()
        val oldSession = model.uiState.value.monitoringSession
        model.onVibration(vibration(1), oldSession)
        model.stopMonitoring()
        assertNull(model.uiState.value.vibration)
        assertTrue(model.uiState.value.vibrationHistory.isEmpty())
        model.toggleMonitoring()
        model.onVibration(vibration(2), oldSession)
        model.sensorFailed(SensorProblem.READ_FAILED, oldSession)
        assertNull(model.uiState.value.vibration)
        assertNull(model.uiState.value.sensorProblem)
        model.onVibration(vibration(3), model.uiState.value.monitoringSession)
        assertEquals(vibration(3), model.uiState.value.vibration)
    }

    @Test fun missingSensorKeepsVisualMonitoringActive() {
        val model = MotionXViewModel()
        model.setCameraReady(true)
        model.toggleMonitoring()
        model.onVisualMotion(reading)
        model.sensorFailed(SensorProblem.UNAVAILABLE, model.uiState.value.monitoringSession)
        assertTrue(model.uiState.value.isMonitoring)
        assertEquals(reading, model.uiState.value.visualMotion)
        assertNull(model.uiState.value.vibration)
        assertEquals(SensorProblem.UNAVAILABLE, model.uiState.value.sensorProblem)
    }

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
