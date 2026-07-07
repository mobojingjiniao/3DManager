package com.threed.manager.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.CoroutineScope

/**
 * Real [SensorSource] backed by Android [SensorManager].
 *
 * Phase 2.4 (later) wires the full lifecycle. This class compiles and
 * can be unit-tested against a Robolectric shadow; the production wire-up
 * (Application start, register/unregister) is intentionally deferred to
 * avoid pulling in Hilt here.
 *
 * Sensors used:
 *  - [Sensor.TYPE_GAME_ROTATION_VECTOR] for [rotation] (no geomagnetic
 *    drift, no magnetometer dependence, recommended for 3D rendering).
 *  - [Sensor.TYPE_GRAVITY] for [gravity] (filtered by the system; we
 *    apply our own [GravityFilter] for additional smoothing if needed).
 *
 * Threading:
 *  - `registerListener` runs on the calling coroutine; events dispatch
 *    on the sensor thread (HandlerThread under the hood).
 *  - [callbackFlow] forwards to a [MutableSharedFlow] so downstream
 *    consumers can re-subscribe without re-registering the sensor.
 */
class AndroidSensorSource(
    private val context: Context,
    private val smoothingAlpha: Float = GravityFilter.DEFAULT_ALPHA,
    private val externalScope: CoroutineScope,
) : SensorSource {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val gravityFilter = GravityFilter(smoothingAlpha)

    // Direct shared flows (sensors are hot).
    private val gravityFlow = MutableSharedFlow<GravityVector>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val rotationFlow = MutableSharedFlow<RotationVector>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val gravity: Flow<GravityVector> = gravityFlow
    override val rotation: Flow<RotationVector> = rotationFlow

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GRAVITY -> {
                    val raw = GravityVector(event.values[0], event.values[1], event.values[2])
                    gravityFlow.tryEmit(gravityFilter.process(raw))
                }
                Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                    val v = event.values
                    // v[3] is cos(θ/2) when calibrated; copy directly.
                    val q = if (v.size >= 4) {
                        RotationVector(v[0], v[1], v[2], v[3])
                    } else {
                        // Fallback: estimate w for older APIs
                        val w2 = 1f - (v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
                        RotationVector(v[0], v[1], v[2], kotlin.math.sqrt(w2.coerceAtLeast(0f)))
                    }
                    rotationFlow.tryEmit(q)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private var started = false

    /** Begin receiving sensor events. Idempotent. */
    fun start() {
        if (started) return
        started = true
        gravitySensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        if (!started) return
        started = false
        sensorManager.unregisterListener(listener)
        gravityFilter.reset()
    }
}
