package com.threed.manager.core.sensor

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Phase 2.2 — sanity test for the [FakeSensorSource] test double.
 *
 * Confirms the shared flow contract (replay=1, dropOldest) holds.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeSensorSourceTest {

    @Test
    fun `emitGravity pushes value to gravity flow`() = runTest {
        val source = FakeSensorSource()
        source.emitGravity(GravityVector(0f, 0f, -9.81f))
        val received = source.gravity.first()
        assertThat(received.z).isEqualTo(-9.81f)
    }

    @Test
    fun `emitRotation pushes quaternion to rotation flow`() = runTest {
        val source = FakeSensorSource()
        source.emitRotation(RotationVector(0f, 0f, 0f, 1f))
        val received = source.rotation.first()
        assertThat(received).isEqualTo(RotationVector(0f, 0f, 0f, 1f))
    }

    @Test
    fun `latest emitted value wins on re-subscribe (replay 1)`() = runTest {
        val source = FakeSensorSource()
        source.emitGravity(GravityVector(1f, 2f, 3f))
        source.emitGravity(GravityVector(4f, 5f, 6f))
        val received = source.gravity.first()
        assertThat(received).isEqualTo(GravityVector(4f, 5f, 6f))
    }

    @Test
    fun `stop is idempotent and invalidates the source`() = runTest {
        val source = FakeSensorSource()
        source.stop()
        source.stop()  // should not throw
        // subsequent emit throws
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            source.emitGravity(GravityVector.Zero)
        }
    }
}
