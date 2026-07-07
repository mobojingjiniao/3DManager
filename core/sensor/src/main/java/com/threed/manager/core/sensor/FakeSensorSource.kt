package com.threed.manager.core.sensor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-memory [SensorSource] for tests and Maestro-driven E2E flows.
 *
 * Threads safely: backed by a [MutableSharedFlow] with `extraBufferCapacity`
 * to absorb producer bursts. Tests call [emitGravity] / [emitRotation]
 * directly; no SensorManager mock required (Robolectric would need a lot
 * of plumbing for a small win).
 */
class FakeSensorSource : SensorSource {

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

    override val gravity: Flow<GravityVector> = gravityFlow.asSharedFlow()
    override val rotation: Flow<RotationVector> = rotationFlow.asSharedFlow()

    private var stopped = false

    fun emitGravity(value: GravityVector) {
        check(!stopped) { "FakeSensorSource has been stopped" }
        gravityFlow.tryEmit(value)
    }

    fun emitRotation(value: RotationVector) {
        check(!stopped) { "FakeSensorSource has been stopped" }
        rotationFlow.tryEmit(value)
    }

    override fun stop() {
        stopped = true
        gravityFlow.resetReplayCache()
        rotationFlow.resetReplayCache()
    }
}
