package com.motionx.app.camera

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.transform.CoordinateTransform
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.motionx.app.model.CameraProblem
import com.motionx.app.model.VisualMotionData
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
@androidx.annotation.OptIn(androidx.camera.view.TransformExperimental::class)
fun CameraPreview(
    monitoring: Boolean,
    onReady: (Boolean) -> Unit,
    onReading: (VisualMotionData) -> Unit,
    onError: (CameraProblem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val readyCallback by rememberUpdatedState(onReady)
    val readingCallback by rememberUpdatedState(onReading)
    val errorCallback by rememberUpdatedState(onError)
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    var layoutSize by remember { mutableStateOf(IntSize.Zero) }
    var point by remember { mutableStateOf<Offset?>(null) }

    DisposableEffect(previewView, owner, layoutSize, monitoring) {
        val active = AtomicBoolean(true)
        val main = ContextCompat.getMainExecutor(context)
        val executor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var camera: Camera? = null
        val preview = Preview.Builder().build()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
            ).build()).build()
        point = null
        readyCallback(false)
        val streamObserver = Observer<PreviewView.StreamState> {
            if (active.get()) readyCallback(it == PreviewView.StreamState.STREAMING)
        }
        val cameraObserver = Observer<CameraState> {
            if (active.get() && it.error != null) errorCallback(CameraProblem.OPEN_FAILED)
        }
        previewView.previewStreamState.observe(owner, streamObserver)
        if (monitoring) {
            analysis.setAnalyzer(executor, CameraAnalyzer(onResult = { data, source ->
                main.execute {
                    if (active.get()) {
                        val target = previewView.outputTransform
                        point = if (data.isTracking && target != null) {
                            val coordinates = floatArrayOf(data.x, data.y)
                            CoordinateTransform(source, target).mapPoints(coordinates)
                            Offset(coordinates[0], coordinates[1])
                        } else null
                        readingCallback(data)
                    }
                }
            }, onError = {
                main.execute { if (active.get()) errorCallback(CameraProblem.ANALYSIS_FAILED) }
            }))
        }
        if (layoutSize != IntSize.Zero) {
            future.addListener({
                if (active.get()) {
                    try {
                        provider = future.get()
                        if (provider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) != true) {
                            errorCallback(CameraProblem.UNAVAILABLE)
                        } else {
                            previewView.doOnLayout {
                                if (active.get()) {
                                    try {
                                        val viewport = requireNotNull(previewView.viewPort)
                                        preview.setSurfaceProvider(previewView.surfaceProvider)
                                        val group = UseCaseGroup.Builder().setViewPort(viewport).addUseCase(preview)
                                        if (monitoring) group.addUseCase(analysis)
                                        camera = provider?.bindToLifecycle(owner,
                                            CameraSelector.DEFAULT_BACK_CAMERA, group.build())
                                        camera?.cameraInfo?.cameraState?.observe(owner, cameraObserver)
                                    } catch (_: Exception) {
                                        errorCallback(CameraProblem.OPEN_FAILED)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        errorCallback(CameraProblem.OPEN_FAILED)
                    }
                }
            }, main)
        }
        onDispose {
            active.set(false)
            previewView.previewStreamState.removeObserver(streamObserver)
            camera?.cameraInfo?.cameraState?.removeObserver(cameraObserver)
            analysis.clearAnalyzer()
            provider?.unbind(preview, analysis)
            executor.shutdown()
            readyCallback(false)
        }
    }

    Box(modifier.clipToBounds().onSizeChanged { layoutSize = it }) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            val tracked = point
            if (monitoring && tracked != null) {
                drawCircle(Color(0xFF7EDCC7), 18.dp.toPx(), tracked, style = Stroke(2.dp.toPx()))
                drawLine(Color(0xFF7EDCC7), tracked - Offset(7.dp.toPx(), 0f),
                    tracked + Offset(7.dp.toPx(), 0f), 2.dp.toPx())
                drawLine(Color(0xFF7EDCC7), tracked - Offset(0f, 7.dp.toPx()),
                    tracked + Offset(0f, 7.dp.toPx()), 2.dp.toPx())
            } else {
                drawCircle(Color.White.copy(alpha = 0.6f), size.minDimension * 0.3f,
                    center, style = Stroke(1.dp.toPx()))
            }
        }
    }
}
