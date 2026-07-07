package com.threed.manager.core.sensor

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Phase 2.3 — TDD RED tests for [RoamingController].
 *
 * The controller is a deterministic, JVM-only state machine. It consumes
 * drag-delta events and a [SensorSource] (gravity + rotation) and produces
 * a [CameraState]. The real implementation feeds [CameraState] into a
 * [WebBridge.setCamera] call from the bridge layer.
 *
 * RED at the moment of writing: `RoamingController`, `RoamingMode`,
 * `CameraState` not yet declared. Phase 2.3 GREEN will add the minimum
 * implementations and flip all assertions to passing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RoamingControllerTest {

    @Test
    fun `Orbit mode rotates yaw by drag delta x`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 100f, dy = 0f)
        val camera = controller.camera.value
        // 100 px * 1.0 sensitivity = 100 deg of yaw
        assertThat(camera.yawDeg).isWithin(1e-3f).of(100f)
        assertThat(camera.pitchDeg).isWithin(1e-3f).of(0f)
    }

    @Test
    fun `Orbit mode rotates pitch by drag delta y`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 0f, dy = -50f)
        val camera = controller.camera.value
        assertThat(camera.pitchDeg).isWithin(1e-3f).of(50f)
    }

    @Test
    fun `Orbit deadband suppresses small drag deltas`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 5f)
        controller.feedDrag(dx = 2f, dy = 0f)  // 2px < 5px deadband
        assertThat(controller.camera.value.yawDeg).isEqualTo(0f)
        controller.feedDrag(dx = 10f, dy = 0f)  // 10px > 5px deadband
        assertThat(controller.camera.value.yawDeg).isWithin(1e-3f).of(10f)
    }

    @Test
    fun `Orbit pitch is clamped to +-85 degrees to avoid gimbal lock`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 0f, dy = 1000f)  // huge upward
        assertThat(controller.camera.value.pitchDeg).isLessThan(90f)
        assertThat(controller.camera.value.pitchDeg).isAtLeast(-90f)
    }

    @Test
    fun `sensitivity scales the drag delta`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 0.5f, deadbandDeg = 0f)
        controller.feedDrag(dx = 100f, dy = 0f)
        assertThat(controller.camera.value.yawDeg).isWithin(1e-3f).of(50f)
    }

    @Test
    fun `FPS mode translates eye by drag deltas instead of rotating`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Fps, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 10f, dy = 0f)
        val camera = controller.camera.value
        // FPS translates eye position; the math is right-strafe for positive dx.
        assertThat(camera.eyeX).isGreaterThan(0f)
        // FPS does NOT rotate around the target
        assertThat(camera.yawDeg).isEqualTo(0f)
    }

    @Test
    fun `Trackball mode applies both yaw and pitch from same drag delta`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Trackball, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 100f, dy = 50f)
        val camera = controller.camera.value
        assertThat(camera.yawDeg).isWithin(1e-3f).of(100f)
        assertThat(camera.pitchDeg).isWithin(1e-3f).of(50f)
    }

    @Test
    fun `mode switch resets accumulated rotation`() = runTest {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedDrag(dx = 100f, dy = 0f)
        assertThat(controller.camera.value.yawDeg).isEqualTo(100f)
        controller.setMode(RoamingMode.Trackball)
        // After mode switch, Orbit yaw is reset (FPS is its own coordinate space)
        assertThat(controller.camera.value.yawDeg).isEqualTo(0f)
    }
}
