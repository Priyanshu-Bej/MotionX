package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.MotionXUiState
import com.motionx.app.model.SensorProblem
import com.motionx.app.model.VibrationStatus
import com.motionx.app.ui.theme.MotionXTheme

@Composable
fun MainScreen(
    state: MotionXUiState,
    modifier: Modifier = Modifier,
    onToggleMonitoring: () -> Unit = {},
    cameraContent: @Composable () -> Unit = { Text(stringResource(R.string.camera_title)) },
) {
    var showGuide by rememberSaveable { mutableStateOf(false) }
    if (showGuide) MeasurementGuide(onDismiss = { showGuide = false })
    Scaffold(modifier = modifier) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { showGuide = true }) {
                    Text(stringResource(R.string.measurement_guide))
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.camera_title), style = MaterialTheme.typography.labelSmall)
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        cameraContent()
                    }
                }
            }
            Text(stringResource(R.string.marker_instructions), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(when {
                state.cameraProblem != null -> R.string.camera_error_status
                state.isMonitoring && state.visualMotion?.isTracking == true -> R.string.tracking_status
                state.isMonitoring -> R.string.searching_status
                state.cameraReady -> R.string.ready_status
                else -> R.string.status_idle
            }), style = MaterialTheme.typography.labelLarge)
            Button(onClick = onToggleMonitoring,
                enabled = state.isMonitoring || state.cameraReady,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (state.isMonitoring) R.string.stop_monitoring else R.string.start_monitoring))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(stringResource(R.string.visual_motion),
                    state.visualMotion?.takeIf { it.isTracking }?.displacement,
                    stringResource(R.string.unit_pixels), Modifier.weight(1f),
                    description = stringResource(R.string.visual_motion_hint))
                MeasurementCard(stringResource(R.string.physical_vibration), state.vibration?.magnitude,
                    stringResource(R.string.unit_g), Modifier.weight(1f),
                    description = stringResource(R.string.physical_vibration_hint))
            }
            Text(stringResource(R.string.g_explanation), style = MaterialTheme.typography.bodySmall)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.visual_graph_title), style = MaterialTheme.typography.labelSmall)
                    VisualMotionGraph(state.visualMotionHistory)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(stringResource(R.string.position_x), state.visualMotion?.takeIf { it.isTracking }?.x,
                    stringResource(R.string.unit_pixels), Modifier.weight(1f),
                    description = stringResource(R.string.position_hint))
                MeasurementCard(stringResource(R.string.position_y), state.visualMotion?.takeIf { it.isTracking }?.y,
                    stringResource(R.string.unit_pixels), Modifier.weight(1f),
                    description = stringResource(R.string.position_hint))
            }
            Text(stringResource(R.string.reference_explanation), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(stringResource(R.string.rms), state.vibration?.rms,
                    stringResource(R.string.unit_g), Modifier.weight(1f),
                    description = stringResource(R.string.rms_hint))
                MeasurementCard(stringResource(R.string.peak), state.vibration?.peak,
                    stringResource(R.string.unit_g), Modifier.weight(1f),
                    description = stringResource(R.string.peak_hint))
            }
            val vibrationStatus = state.vibration?.status
            Text(stringResource(when {
                state.sensorProblem == SensorProblem.UNAVAILABLE -> R.string.sensor_unavailable
                state.sensorProblem == SensorProblem.READ_FAILED -> R.string.sensor_error
                !state.isMonitoring -> R.string.sensor_idle
                vibrationStatus == VibrationStatus.HIGH_VIBRATION -> R.string.vibration_high
                vibrationStatus == VibrationStatus.VIBRATING -> R.string.vibration_warning
                vibrationStatus == VibrationStatus.NORMAL -> R.string.vibration_normal
                else -> R.string.waiting_for_samples
            }), color = when (vibrationStatus) {
                VibrationStatus.HIGH_VIBRATION -> MaterialTheme.colorScheme.error
                VibrationStatus.VIBRATING -> Color(0xFFFFCA70)
                VibrationStatus.NORMAL -> MaterialTheme.colorScheme.primary
                null -> MaterialTheme.colorScheme.onSurfaceVariant
            }, style = MaterialTheme.typography.labelLarge)
            Text(vibrationThresholdExplanation(), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.vibration?.let { reading ->
                Text(stringResource(R.string.raw_axes, reading.accelerationX,
                    reading.accelerationY, reading.accelerationZ), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.raw_axes_hint), style = MaterialTheme.typography.bodySmall)
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.graph_title), style = MaterialTheme.typography.labelSmall)
                    VibrationGraph(state.vibrationHistory)
                }
            }
            Text(stringResource(R.string.sensor_explanation),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun MainScreenPreview() {
    MotionXTheme { MainScreen(MotionXUiState()) }
}
