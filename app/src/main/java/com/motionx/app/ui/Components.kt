package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import java.util.Locale

@Composable
fun MeasurementCard(
    label: String,
    value: Float?,
    unit: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value?.let { String.format(Locale.getDefault(), "%.2f", it) }
                    ?: stringResource(R.string.unavailable_value),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(unit, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
