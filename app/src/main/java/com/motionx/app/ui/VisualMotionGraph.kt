package com.motionx.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.VisualMotionData

/** Plots displacement in buffer pixels; each reacquired marker starts a new segment. */
@Composable
fun VisualMotionGraph(readings: List<VisualMotionData>) {
    val valid = readings.filter { it.isTracking }
    if (valid.isEmpty()) {
        Text(stringResource(R.string.visual_graph_waiting), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val ceiling = maxOf(1f, valid.maxOf { it.displacement } * 1.1f)
    val trace = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.visual_graph_description)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.visual_graph_scale, ceiling), style = MaterialTheme.typography.labelSmall)
        Canvas(Modifier.fillMaxWidth().height(100.dp).padding(3.dp)
            .semantics { contentDescription = description }) {
            for (level in 0..4) {
                val y = size.height * level / 4
                drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            val end = readings.last().timestamp
            val start = end - 10_000_000_000L
            val path = Path()
            var previous: VisualMotionData? = null
            readings.forEach { data ->
                if (!data.isTracking) {
                    previous = null
                } else {
                    val x = ((data.timestamp - start) / 10_000_000_000f).coerceIn(0f, 1f) * size.width
                    val y = size.height * (1f - (data.displacement / ceiling).coerceIn(0f, 1f))
                    val prior = previous
                    if (prior == null || data.timestamp - prior.timestamp > 500_000_000L) {
                        path.moveTo(x, y)
                        drawCircle(trace, 2.dp.toPx(), Offset(x, y))
                    } else path.lineTo(x, y)
                    previous = data
                }
            }
            drawPath(path, trace, style = Stroke(2.dp.toPx()))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.graph_ten_seconds_ago), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.graph_now), style = MaterialTheme.typography.labelSmall)
        }
        if (!readings.last().isTracking) {
            Text(stringResource(R.string.visual_graph_lost), style = MaterialTheme.typography.bodySmall)
        }
    }
}
