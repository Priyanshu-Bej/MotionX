package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.AlertChannel
import com.motionx.app.model.ThresholdAlertSettings

@Composable
internal fun ThresholdAlertControls(
    channel: AlertChannel,
    settings: ThresholdAlertSettings,
    onChange: (ThresholdAlertSettings) -> Unit,
) {
    var input by rememberSaveable(settings.threshold) { mutableStateOf(settings.threshold.toString()) }
    val parsed = input.trim().replace(',', '.').toFloatOrNull()
    val valid = parsed != null && settings.copy(threshold = parsed).isValid(channel)
    val focus = LocalFocusManager.current
    val unit = stringResource(if (channel == AlertChannel.VISUAL) R.string.unit_pixels_name else R.string.unit_g_name)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AlertSwitch(stringResource(if (channel == AlertChannel.VISUAL)
                R.string.alert_visual_enable else R.string.alert_physical_enable), settings.enabled) {
                onChange(settings.copy(enabled = it))
            }
            Text(stringResource(if (channel == AlertChannel.VISUAL)
                R.string.alert_visual_metric else R.string.alert_physical_metric), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = input, onValueChange = { input = it }, singleLine = true,
                label = { Text(stringResource(R.string.alert_threshold, unit)) },
                modifier = Modifier.fillMaxWidth(), isError = !valid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text(stringResource(if (valid) R.string.alert_applied_value else R.string.alert_invalid,
                    if (valid) settings.threshold else channel.maxThreshold, unit)) })
            Button(enabled = valid && parsed != settings.threshold, onClick = {
                if (valid) {
                    onChange(settings.copy(threshold = parsed))
                    focus.clearFocus()
                }
            }) { Text(stringResource(R.string.alert_apply)) }
            AlertSwitch(stringResource(R.string.alert_sound), settings.sound) { onChange(settings.copy(sound = it)) }
            AlertSwitch(stringResource(R.string.alert_vibrate), settings.vibrate) { onChange(settings.copy(vibrate = it)) }
            Text(stringResource(R.string.alert_feedback_note), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AlertSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        Switch(checked = checked, onCheckedChange = onChange,
            modifier = Modifier.semantics { contentDescription = label })
    }
}
