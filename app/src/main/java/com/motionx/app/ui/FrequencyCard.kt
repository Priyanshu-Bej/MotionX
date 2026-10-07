package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.model.FrequencyData
import com.motionx.app.model.FrequencyStatus

@Composable
internal fun FrequencyCard(frequency: FrequencyData?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MeasurementCard(stringResource(R.string.frequency_title), frequency?.hz,
            stringResource(R.string.unit_hz), Modifier.fillMaxWidth(), decimalPlaces = 1,
            description = stringResource(when (frequency?.status) {
                FrequencyStatus.COLLECTING -> R.string.frequency_collecting
                FrequencyStatus.LOW_SIGNAL -> R.string.frequency_weak
                FrequencyStatus.NO_CLEAR_PEAK -> R.string.frequency_unclear
                FrequencyStatus.READY -> R.string.frequency_ready
                null -> R.string.frequency_idle
            }))
        if (frequency?.sampleRateHz != null && frequency.upperLimitHz != null) {
            Text(stringResource(R.string.frequency_sampling, frequency.sampleRateHz,
                frequency.upperLimitHz), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
