package com.motionx.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.VibrationData

@Composable
fun VibrationGraph(readings: List<VibrationData>, threshold: Float? = null) {
    if (readings.size < 2) {
        Text(stringResource(R.string.waiting_for_samples), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val ceiling = maxOf(0.2f, (threshold ?: 0f) * 1.1f, readings.maxOf { it.magnitude } * 1.1f)
    val thresholdColor = MaterialTheme.colorScheme.error
    val trace = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Column {
        Text(stringResource(R.string.graph_scale, ceiling), style = MaterialTheme.typography.labelSmall)
        threshold?.let {
            Text(stringResource(R.string.alert_graph_line, it, "g"),
                style = MaterialTheme.typography.labelSmall, color = thresholdColor)
        }
        Canvas(Modifier.fillMaxWidth().height(100.dp)) {
            for (level in 0..4) {
                val y = size.height * level / 4
                drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            threshold?.let {
                val y = size.height * (1f - (it / ceiling).coerceIn(0f, 1f))
                drawLine(thresholdColor, Offset(0f, y), Offset(size.width, y), 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx())))
            }
            val end = readings.last().timestamp
            val start = end - 10_000_000_000L
            val path = Path()
            readings.forEachIndexed { index, data ->
                val x = ((data.timestamp - start) / 10_000_000_000f).coerceIn(0f, 1f) * size.width
                val y = size.height * (1f - (data.magnitude / ceiling).coerceIn(0f, 1f))
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, trace, style = Stroke(2.dp.toPx()))
        }
    }
}
