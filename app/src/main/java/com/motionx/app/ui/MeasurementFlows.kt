package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.motionx.app.R

/** Explanatory diagrams only; these do not control or observe the live sensor session. */
@Composable
internal fun MeasurementFlows() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.flow_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.flow_shared), style = MaterialTheme.typography.bodyMedium)
        FlowSection(stringResource(R.string.flow_visual_title)) {
            FlowNode(R.string.flow_camera)
            FlowArrow()
            FlowNode(R.string.flow_marker)
            FlowArrow()
            FlowNode(R.string.flow_displacement)
            FlowArrow()
            FlowNode(R.string.flow_visual_output)
            FlowNote(R.string.flow_tracking_lost)
        }
        FlowSection(stringResource(R.string.flow_physical_title)) {
            FlowNode(R.string.flow_accelerometer)
            FlowArrow()
            FlowNode(R.string.flow_gravity)
            FlowArrow()
            Text(stringResource(R.string.flow_parallel), style = MaterialTheme.typography.labelLarge)
            FlowNode(R.string.flow_magnitude)
            FlowNode(R.string.flow_rms)
            FlowNode(R.string.flow_hz)
            FlowNote(R.string.flow_physical_note)
        }
        FlowSection(stringResource(R.string.flow_alert_title)) {
            FlowNode(R.string.flow_alert_input)
            FlowArrow()
            FlowNode(R.string.flow_alert_decision)
            FlowArrow()
            FlowNode(R.string.flow_alert_output)
            FlowArrow()
            FlowNode(R.string.flow_alert_rearm)
            FlowNote(R.string.flow_stop)
        }
    }
}

@Composable
private fun FlowSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(Modifier.fillMaxWidth()) {
        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (expanded) R.string.flow_hide else R.string.flow_show, title),
                textAlign = TextAlign.Center)
        }
        if (expanded) {
            Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, content = content)
        }
    }
}

@Composable
private fun FlowNode(text: Int) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(stringResource(text), modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FlowArrow() {
    Text("↓", modifier = Modifier.clearAndSetSemantics { },
        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun FlowNote(text: Int) {
    Text(stringResource(text), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}
