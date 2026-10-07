package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.MotionXUiState
import com.motionx.app.model.AlertChannel
import com.motionx.app.model.ThresholdAlertSettings
import com.motionx.app.model.SensorProblem
import com.motionx.app.model.VibrationStatus
import com.motionx.app.ui.theme.MotionXTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    state: MotionXUiState,
    modifier: Modifier = Modifier,
    onToggleMonitoring: () -> Unit = {},
    onAlertChange: (AlertChannel, ThresholdAlertSettings) -> Unit = { _, _ -> },
    lastAlert: Set<AlertChannel> = emptySet(),
    cameraContent: @Composable () -> Unit = { Text(stringResource(R.string.camera_title)) },
) {
    val pager = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val tabs = listOf(R.string.tab_visual, R.string.tab_physical, R.string.tab_guide)
    val tabSensors = listOf(R.string.tab_sensor_camera, R.string.tab_sensor_accelerometer, null)
    Scaffold(modifier = modifier) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TabRow(selectedTabIndex = pager.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = pager.currentPage == index,
                        onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(title))
                                tabSensors[index]?.let { sensor ->
                                    Text(stringResource(sensor),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center)
                                }
                            }
                        })
                }
            }
            // Retain all three pages: removing CameraPreview would unbind the camera,
            // reset tracking, and interrupt the shared session on a tab switch.
            HorizontalPager(state = pager, beyondViewportPageCount = 2,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top) { page ->
                when (page) {
                    0 -> VisualTab(state, { onAlertChange(AlertChannel.VISUAL, it) }, cameraContent)
                    1 -> PhysicalTab(state) { onAlertChange(AlertChannel.PHYSICAL, it) }
                    2 -> MeasurementGuide(Modifier.fillMaxSize().padding(20.dp))
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (lastAlert.isNotEmpty()) {
                    Text(stringResource(when {
                        lastAlert.size == 2 -> R.string.alert_both_triggered
                        AlertChannel.VISUAL in lastAlert -> R.string.alert_visual_triggered
                        else -> R.string.alert_physical_triggered
                    }), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
                Text(stringResource(when {
                    state.cameraProblem != null -> R.string.camera_error_status
                    state.isMonitoring && state.visualMotion?.isTracking == true -> R.string.tracking_status
                    state.isMonitoring -> R.string.searching_status
                    state.cameraReady -> R.string.ready_status
                    else -> R.string.status_idle
                }), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(if (!state.cameraReady && !state.isMonitoring)
                    R.string.tabs_camera_setup else R.string.tabs_shared_monitoring),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onToggleMonitoring,
                    enabled = state.isMonitoring || state.cameraReady,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.isMonitoring) R.string.stop_monitoring else R.string.start_monitoring))
                }
            }
        }
    }
}

@Composable
private fun VisualTab(state: MotionXUiState, onAlertChange: (ThresholdAlertSettings) -> Unit,
    cameraContent: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        MeasurementCard(stringResource(R.string.visual_motion),
            state.visualMotion?.takeIf { it.isTracking }?.displacement,
            stringResource(R.string.unit_pixels), Modifier.fillMaxWidth(),
            description = stringResource(R.string.visual_motion_hint))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.visual_graph_title), style = MaterialTheme.typography.labelSmall)
                VisualMotionGraph(state.visualMotionHistory, state.visualAlert.takeIf { it.enabled }?.threshold)
            }
        }
        ThresholdAlertControls(AlertChannel.VISUAL, state.visualAlert, onAlertChange)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeasurementCard(stringResource(R.string.position_x), state.visualMotion?.takeIf { it.isTracking }?.x,
                stringResource(R.string.unit_pixels), Modifier.weight(1f),
                description = stringResource(R.string.position_hint))
            MeasurementCard(stringResource(R.string.position_y), state.visualMotion?.takeIf { it.isTracking }?.y,
                stringResource(R.string.unit_pixels), Modifier.weight(1f),
                description = stringResource(R.string.position_hint))
        }
        Text(stringResource(R.string.reference_explanation), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PhysicalTab(state: MotionXUiState, onAlertChange: (ThresholdAlertSettings) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MeasurementCard(stringResource(R.string.physical_vibration), state.vibration?.magnitude,
            stringResource(R.string.unit_g), Modifier.fillMaxWidth(),
            description = stringResource(R.string.physical_vibration_hint))
        Text(stringResource(R.string.g_explanation), style = MaterialTheme.typography.bodySmall)
        FrequencyCard(state.vibration?.frequency)
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
                VibrationGraph(state.vibrationHistory, state.physicalAlert.takeIf { it.enabled }?.threshold)
            }
        }
        ThresholdAlertControls(AlertChannel.PHYSICAL, state.physicalAlert, onAlertChange)
        Text(stringResource(R.string.sensor_explanation),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun MainScreenPreview() {
    MotionXTheme { MainScreen(MotionXUiState()) }
}
