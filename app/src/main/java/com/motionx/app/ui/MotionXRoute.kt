package com.motionx.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.motionx.app.R
import com.motionx.app.camera.CameraPreview
import com.motionx.app.model.CameraProblem
import com.motionx.app.model.SensorProblem
import com.motionx.app.sensors.AccelerometerSource
import com.motionx.app.sensors.SensorUnavailableException
import com.motionx.app.viewmodel.MotionXViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate

@Composable
fun MotionXRoute(viewModel: MotionXViewModel = viewModel()) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    var permission by remember { mutableStateOf(hasPermission()) }
    var resumed by remember { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accelerometer = remember(context.applicationContext) { AccelerometerSource(context.applicationContext) }
    LaunchedEffect(state.isMonitoring, state.monitoringSession, resumed) {
        if (state.isMonitoring && resumed) {
            val session = state.monitoringSession
            accelerometer.readings().conflate().catch { failure ->
                viewModel.sensorFailed(if (failure is SensorUnavailableException)
                    SensorProblem.UNAVAILABLE else SensorProblem.READ_FAILED, session)
            }.collect { viewModel.onVibration(it, session) }
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permission = it
        if (!it) viewModel.stopMonitoring()
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { permission = hasPermission(); resumed = true }
                Lifecycle.Event.ON_PAUSE -> { resumed = false; viewModel.stopMonitoring() }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); viewModel.stopMonitoring() }
    }
    MainScreen(state, onToggleMonitoring = viewModel::toggleMonitoring, cameraContent = {
        when {
            !permission -> Column(Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.camera_permission_explanation))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text(stringResource(R.string.allow_camera))
                }
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:${context.packageName}".toUri()))
                }) { Text(stringResource(R.string.open_settings)) }
            }
            state.cameraProblem != null -> Column(Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(when (state.cameraProblem) {
                    CameraProblem.UNAVAILABLE -> R.string.camera_unavailable
                    CameraProblem.ANALYSIS_FAILED -> R.string.camera_analysis_error
                    else -> R.string.camera_open_error
                }))
                Button(onClick = viewModel::retryCamera) { Text(stringResource(R.string.retry_camera)) }
            }
            resumed -> CameraPreview(state.isMonitoring, viewModel::setCameraReady,
                viewModel::onVisualMotion, viewModel::cameraFailed, Modifier.fillMaxSize())
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.camera_paused))
            }
        }
    })
}
