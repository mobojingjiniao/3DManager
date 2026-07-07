package com.threed.manager.core.sensor

import kotlinx.coroutines.flow.Flow

/**
 * Abstract, lifecycle-aware source of device sensor data.
 *
 * Backed by either:
 *  - [AndroidSensorSource]  (real SensorManager)
 *  - [FakeSensorSource]     (tests + Maestro-driven E2E)
 *
 * The interface is intentionally narrow (only the two flows we need) so
 * implementations are easy to fake and the consumer — typically a
 * [RoamingController] — can swap sources without changing its code.
 *
 * Threading contract:
 *  - Implementations emit on a background dispatcher.
 *  - The flow is conflated-by-default; missed samples are dropped, the
 *    receiver always sees the latest.
 *  - No back-pressure is needed (sample rate ≤ 200 Hz).
 *
 * Lifecycle:
 *  - [Flow] is hot by design; collecting starts the sensor, the upstream
 *    cancellation calls [SensorSource.stop]. The contract is honored by
 *    [AndroidSensorSource] and [FakeSensorSource].
 */
interface SensorSource {

    /** Smoothed gravity vector in device frame, m/s². */
    val gravity: Flow<GravityVector>

    /** Game rotation vector (quaternion). Avoids gimbal lock. */
    val rotation: Flow<RotationVector>

    /**
     * Permanently stop emitting. The [Flow] will complete.
     * Idempotent: calling twice is a no-op.
     */
    fun stop()
}
