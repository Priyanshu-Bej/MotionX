package com.motionx.app.camera

import com.motionx.app.model.VisualMotionData
import java.nio.ByteBuffer
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** Lightweight dark-marker detector. Single analysis-thread ownership. No Android dependencies. */
class MarkerTracker {
    private data class Marker(val x: Float, val y: Float, val area: Int)
    private var previous: Marker? = null
    private var origin: Marker? = null

    fun reset() { previous = null; origin = null }

    fun track(
        luminance: ByteBuffer, rowStride: Int, pixelStride: Int,
        left: Int, top: Int, width: Int, height: Int, timestamp: Long,
    ): VisualMotionData {
        val step = max(1, max(width, height) / 320)
        val columns = width / step
        val rows = height / step
        if (columns < 12 || rows < 12) return lost(timestamp)
        val values = IntArray(columns * rows)
        var darkest = 255
        val histogram = IntArray(256)
        val base = luminance.position()
        for (y in 0 until rows) for (x in 0 until columns) {
            val value = luminance.get(base + (top + y * step) * rowStride +
                (left + x * step) * pixelStride).toInt() and 255
            values[y * columns + x] = value
            darkest = min(darkest, value)
            histogram[value]++
        }
        var cumulative = 0
        val bright = histogram.indices.first { cumulative += histogram[it]; cumulative >= values.size * 0.9 }
        if (bright - darkest < 60) return lost(timestamp)
        val threshold = darkest + (bright - darkest) * 0.4f
        val visited = BooleanArray(values.size)
        val queue = IntArray(values.size)
        val candidates = mutableListOf<Marker>()
        for (seed in values.indices) {
            if (visited[seed] || values[seed] > threshold) continue
            var head = 0
            var tail = 1
            queue[0] = seed
            visited[seed] = true
            var minX = columns; var maxX = 0; var minY = rows; var maxY = 0
            var sumX = 0.0; var sumY = 0.0; var weightSum = 0.0
            while (head < tail) {
                val index = queue[head++]
                val x = index % columns; val y = index / columns
                minX = min(minX, x); maxX = max(maxX, x)
                minY = min(minY, y); maxY = max(maxY, y)
                val weight = (bright - values[index]).toDouble()
                sumX += x * weight; sumY += y * weight; weightSum += weight
                fun enqueue(next: Int) {
                    if (!visited[next] && values[next] <= threshold) {
                        visited[next] = true
                        queue[tail++] = next
                    }
                }
                if (x > 0) enqueue(index - 1)
                if (x + 1 < columns) enqueue(index + 1)
                if (y > 0) enqueue(index - columns)
                if (y + 1 < rows) enqueue(index + columns)
            }
            val w = maxX - minX + 1; val h = maxY - minY + 1
            // Reject noise, elongated edges, huge dark surfaces, and clipped markers.
            if (tail < 20 || tail > values.size / 8 || w.toFloat() / h !in 0.65f..1.5f ||
                tail.toFloat() / (w * h) < 0.5f || minX < 2 || minY < 2 ||
                maxX >= columns - 2 || maxY >= rows - 2) continue
            var surround = 0; var light = 0
            for (y in minY - 2..maxY + 2) for (x in minX - 2..maxX + 2) {
                if (x < minX || x > maxX || y < minY || y > maxY) {
                    surround++
                    if (values[y * columns + x] > threshold + 20) light++
                }
            }
            if (light < surround * 0.8f) continue
            candidates += Marker(left + (sumX / weightSum).toFloat() * step,
                top + (sumY / weightSum).toFloat() * step, tail)
        }
        val prior = previous
        val targetX = prior?.x ?: (left + width / 2f)
        val targetY = prior?.y ?: (top + height / 2f)
        val radius = min(width, height) * if (prior == null) 0.30f else 0.12f
        val selected = candidates.filter {
            hypot(it.x - targetX, it.y - targetY) <= radius &&
                (prior == null || it.area.toFloat() / prior.area in 0.5f..2f)
        }.minByOrNull { hypot(it.x - targetX, it.y - targetY) } ?: return lost(timestamp)
        previous = selected
        if (origin == null) origin = selected
        val reference = requireNotNull(origin)
        return VisualMotionData(selected.x, selected.y,
            hypot(selected.x - reference.x, selected.y - reference.y), timestamp, true)
    }

    private fun lost(timestamp: Long): VisualMotionData {
        reset()
        return VisualMotionData(0f, 0f, 0f, timestamp, false)
    }
}
