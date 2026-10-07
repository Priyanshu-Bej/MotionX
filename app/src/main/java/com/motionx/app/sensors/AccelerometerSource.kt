package com.motionx.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import com.motionx.app.model.VibrationData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorUnavailableException(message: String) : IllegalStateException(message)

/**
 * Live vibration readings from the device accelerometer.
 *
 * The listener is registered only while [readings] is collected and is unregistered
 * when collection is cancelled, so callers control the sensor's lifetime by starting and
 * stopping collection (for example on start/stop monitoring and foreground/background).
 * Events are delivered on a dedicated background thread, never the main thread.
 */
class AccelerometerSource(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(SensorManager::class.java)
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean get() = accelerometer != null

    /**
     * Each collection starts a fresh [VibrationAnalyzer], so peak and RMS restart per session.
     * Fails with [SensorUnavailableException] if there is no accelerometer or registration fails.
     */
    fun readings(
        config: VibrationAnalyzer.Config = VibrationAnalyzer.Config(),
        samplingPeriodUs: Int = SAMPLING_PERIOD_US,
    ): Flow<VibrationData> = callbackFlow {
        val sensor = accelerometer
        if (sensorManager == null || sensor == null) {
            close(SensorUnavailableException("No accelerometer on this device"))
            return@callbackFlow
        }

        val analyzer = VibrationAnalyzer(config)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                trySend(analyzer.process(x, y, z, event.timestamp))
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val thread = HandlerThread("MotionX-Accelerometer").apply { start() }
        try {
            val registered = sensorManager.registerListener(listener, sensor, samplingPeriodUs, Handler(thread.looper))
            if (!registered) {
                close(SensorUnavailableException("Accelerometer listener registration failed"))
                return@callbackFlow
            }
            awaitClose { }
        } finally {
            // Also clean up if registration throws or collection is cancelled during setup.
            sensorManager.unregisterListener(listener)
            thread.quitSafely()
        }
    }

    companion object {
        /** Requested 100 Hz; the delivered rate is device-dependent and may differ. */
        const val SAMPLING_PERIOD_US = 10_000
    }
}
