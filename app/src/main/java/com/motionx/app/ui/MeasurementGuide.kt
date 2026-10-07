package com.motionx.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionx.app.R
import com.motionx.app.sensors.AccelerometerSource
import com.motionx.app.sensors.VibrationAnalyzer

@Composable
internal fun vibrationThresholdExplanation(): String {
    val config = remember { VibrationAnalyzer.Config() }
    return stringResource(R.string.vibration_thresholds,
        config.vibratingThresholdG, config.highThresholdG)
}

@Composable
internal fun MeasurementGuide(modifier: Modifier = Modifier) {
    val config = remember { VibrationAnalyzer.Config() }
    Column(modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.measurement_guide), style = MaterialTheme.typography.titleLarge)
        GuideSection(stringResource(R.string.guide_g_title), stringResource(R.string.guide_g))
        GuideSection(stringResource(R.string.visual_motion), stringResource(R.string.guide_visual))
        GuideSection(stringResource(R.string.guide_position_title), stringResource(R.string.guide_position))
        GuideSection(stringResource(R.string.physical_vibration), stringResource(R.string.guide_physical))
        GuideSection(stringResource(R.string.rms), stringResource(R.string.guide_rms, config.rmsWindowSec))
        GuideSection(stringResource(R.string.peak), stringResource(R.string.guide_peak, config.warmUpSec))
        GuideSection(stringResource(R.string.guide_status_title), vibrationThresholdExplanation())
        GuideSection(stringResource(R.string.guide_axes_title), stringResource(R.string.guide_axes))
        GuideSection(stringResource(R.string.guide_graphs_title), stringResource(R.string.guide_graphs))
        GuideSection(stringResource(R.string.guide_processing_title), stringResource(
            R.string.guide_processing, 1_000_000 / AccelerometerSource.SAMPLING_PERIOD_US,
            config.gravityTimeConstantSec, config.smoothingTimeConstantSec, config.maxSampleGapSec,
        ))
        GuideSection(stringResource(R.string.guide_missing_title), stringResource(R.string.guide_missing))
    }
}

@Composable
private fun GuideSection(title: String, explanation: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary)
        Text(explanation, style = MaterialTheme.typography.bodyMedium)
    }
}
