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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.MotionXUiState
import com.motionx.app.ui.theme.MotionXTheme

@Composable
fun MainScreen(
    state: MotionXUiState,
    modifier: Modifier = Modifier,
    onToggleMonitoring: () -> Unit = {},
    cameraContent: @Composable () -> Unit = { Text(stringResource(R.string.camera_title)) },
) {
    Scaffold(modifier = modifier) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    stringResource(R.string.unit_pixels), Modifier.weight(1f))
                MeasurementCard(stringResource(R.string.physical_vibration), state.vibration?.magnitude,
                    stringResource(R.string.unit_g), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(stringResource(R.string.position_x), state.visualMotion?.takeIf { it.isTracking }?.x,
                    stringResource(R.string.unit_pixels), Modifier.weight(1f))
                MeasurementCard(stringResource(R.string.position_y), state.visualMotion?.takeIf { it.isTracking }?.y,
                    stringResource(R.string.unit_pixels), Modifier.weight(1f))
            }
            Text(stringResource(R.string.reference_explanation), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(stringResource(R.string.rms), state.vibration?.rms,
                    stringResource(R.string.unit_g), Modifier.weight(1f))
                MeasurementCard(stringResource(R.string.peak), state.vibration?.peak,
                    stringResource(R.string.unit_g), Modifier.weight(1f))
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.graph_title), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.waiting_for_samples), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(stringResource(R.string.sensors_pending),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun MainScreenPreview() {
    MotionXTheme { MainScreen(MotionXUiState()) }
}
