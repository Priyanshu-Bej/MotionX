package com.motionx.app.camera

import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test

class MarkerTrackerTest {
    private fun frame(x: Int = 60, y: Int = 50, dark: Int = 20, light: Int = 230,
        stride: Int = 120, pixelStride: Int = 1, offset: Int = 0): ByteBuffer {
        val data = ByteBuffer.allocate(offset + stride * 100)
        for (py in 0 until 100) for (px in 0 until 120) {
            val inside = (px - x) * (px - x) + (py - y) * (py - y) <= 8 * 8
            data.put(offset + py * stride + px * pixelStride, (if (inside) dark else light).toByte())
        }
        data.position(offset)
        return data
    }
    private fun MarkerTracker.read(data: ByteBuffer, time: Long = 1) =
        track(data, 120, 1, 0, 0, 120, 100, time)

    @Test fun knownTranslationReportsDistanceFromInitialReference() {
        val tracker = MarkerTracker()
        val first = tracker.read(frame())
        assertTrue(first.isTracking)
        assertEquals(60f, first.x, 0.01f)
        assertEquals(50f, first.y, 0.01f)
        assertEquals(0f, first.displacement, 0.01f)
        val moved = tracker.read(frame(66, 58), 2)
        assertTrue(moved.isTracking)
        assertEquals(10f, moved.displacement, 0.01f)
        assertEquals(2L, moved.timestamp)
        val again = tracker.read(frame(69, 62), 3)
        assertEquals(15f, again.displacement, 0.01f)
    }

    @Test fun handlesPaddedRowsPixelStrideBufferPositionAndCropCoordinates() {
        val result = MarkerTracker().track(frame(stride = 256, pixelStride = 2, offset = 7),
            256, 2, 20, 10, 80, 80, 99)
        assertTrue(result.isTracking)
        assertEquals(60f, result.x, 0.01f)
        assertEquals(50f, result.y, 0.01f)
    }

    @Test fun flatLowContrastAndTinyNoiseDoNotBecomeMeasurements() {
        val tracker = MarkerTracker()
        assertFalse(tracker.read(frame(dark = 230)).isTracking)
        assertFalse(tracker.read(frame(dark = 210)).isTracking)
        val noise = frame(dark = 230)
        noise.put(50 * 120 + 60, 0)
        assertFalse(tracker.read(noise).isTracking)
        assertFalse(tracker.read(frame(dark = 0, light = 0)).isTracking)
    }

    @Test fun lossAndReacquisitionResetReferenceWithoutReportingStaleMotion() {
        val tracker = MarkerTracker()
        tracker.read(frame())
        tracker.read(frame(66, 58))
        assertFalse(tracker.read(frame(dark = 230)).isTracking)
        val reacquired = tracker.read(frame(70, 55))
        assertTrue(reacquired.isTracking)
        assertEquals(0f, reacquired.displacement, 0.01f)
    }

    @Test fun rejectsClippedMarkersAndImplausibleJumps() {
        assertFalse(MarkerTracker().read(frame(2, 50)).isTracking)
        val tracker = MarkerTracker()
        tracker.read(frame())
        assertFalse(tracker.read(frame(90, 50)).isTracking)
    }

    @Test fun resetStartsANewMeasurementSession() {
        val tracker = MarkerTracker()
        tracker.read(frame())
        tracker.reset()
        assertEquals(0f, tracker.read(frame(65, 50)).displacement, 0.01f)
    }

    @Test fun downsamplingPreservesFullBufferUnits() {
        fun large(cx: Int): ByteBuffer {
            val bytes = ByteArray(640 * 480) { 230.toByte() }
            for (y in 220..260) for (x in cx - 20..cx + 20) bytes[y * 640 + x] = 20
            return ByteBuffer.wrap(bytes)
        }
        val tracker = MarkerTracker()
        tracker.track(large(320), 640, 1, 0, 0, 640, 480, 1)
        val moved = tracker.track(large(330), 640, 1, 0, 0, 640, 480, 2)
        assertTrue(moved.isTracking)
        assertEquals(330f, moved.x, 0.01f)
        assertEquals(10f, moved.displacement, 0.01f)
    }
}
