package com.motionx.app.model

enum class AlertChannel(val maxThreshold: Float) { VISUAL(10_000f), PHYSICAL(100f) }

data class ThresholdAlertSettings(
    val threshold: Float,
    val enabled: Boolean = false,
    val sound: Boolean = true,
    val vibrate: Boolean = true,
) {
    fun isValid(channel: AlertChannel) = threshold.isFinite() && threshold > 0f && threshold <= channel.maxThreshold
}
