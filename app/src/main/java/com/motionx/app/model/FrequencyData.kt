package com.motionx.app.model

enum class FrequencyStatus { COLLECTING, LOW_SIGNAL, NO_CLEAR_PEAK, READY }

/** Dominant spectral component, not necessarily the fundamental frequency. */
data class FrequencyData(
    val status: FrequencyStatus,
    val hz: Float? = null,
    val sampleRateHz: Float? = null,
    val upperLimitHz: Float? = null,
)
