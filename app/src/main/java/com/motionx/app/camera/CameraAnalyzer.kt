package com.motionx.app.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import com.motionx.app.model.VisualMotionData

@androidx.annotation.OptIn(androidx.camera.view.TransformExperimental::class)
class CameraAnalyzer(
    private val onResult: (VisualMotionData, OutputTransform) -> Unit,
    private val onError: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val tracker = MarkerTracker()
    private val transforms = ImageProxyTransformFactory()
    private var lastTimestamp = Long.MIN_VALUE

    override fun analyze(image: ImageProxy) {
        try {
            val timestamp = image.imageInfo.timestamp
            // At most 15 measurements per second, keeping only the latest camera frame.
            if (lastTimestamp != Long.MIN_VALUE && timestamp - lastTimestamp < 66_666_667L) return
            lastTimestamp = timestamp
            val plane = image.planes[0]
            val crop = image.cropRect
            val data = tracker.track(plane.buffer, plane.rowStride, plane.pixelStride,
                crop.left, crop.top, crop.width(), crop.height(), timestamp)
            // Defaults use full, unrotated buffer coordinates, matching tracker output.
            onResult(data, transforms.getOutputTransform(image))
        } catch (_: Exception) {
            tracker.reset()
            onError()
        } finally {
            image.close()
        }
    }
}
