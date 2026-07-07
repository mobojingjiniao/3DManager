package com.threed.manager.core.sensor

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 2.4 — TDD RED tests for [RoamingController.feedSensor].
 *
 * Wires the device orientation (quaternion from SensorSource.rotation) into
 * the active roaming mode:
 *  - Orbit / Trackball : the rotation rotates the camera around the target
 *  - FPS               : the rotation rotates the look direction (yaw/pitch)
 *
 * Implementation note: full quaternion→Euler math is delegated to a
 * tiny helper ([quaternionToYawPitch]) — we don't depend on a 3D math
 * library, just the standard convention.
 */
class RoamingControllerSensorTest {

    @Test
    fun `feedSensor identity quaternion keeps camera at zero`() {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        controller.feedSensor(RotationVector.Identity)
        val c = controller.camera.value
        assertThat(c.yawDeg).isWithin(1e-3f).of(0f)
        assertThat(c.pitchDeg).isWithin(1e-3f).of(0f)
    }

    @Test
    fun `feedSensor pure yaw quaternion increases yaw in Orbit mode`() {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        // 45° yaw around world Y axis. 90° would put us at a pitch=90°
        // singularity where yaw becomes undefined, so we use a more
        // realistic angle.
        val (x, y, z, w) = quaternionFromAxisAngle(axisX = 0f, axisY = 1f, axisZ = 0f, angleDeg = 45f)
        controller.feedSensor(RotationVector(x, y, z, w))
        val c = controller.camera.value
        assertThat(c.yawDeg).isWithin(1f).of(45f)
    }

    @Test
    fun `feedSensor pure pitch quaternion increases pitch in Orbit mode`() {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        // 30° pitch around world X axis: q = (sin(15°), 0, 0, cos(15°))
        val (x, y, z, w) = quaternionFromAxisAngle(axisX = 1f, axisY = 0f, axisZ = 0f, angleDeg = 30f)
        controller.feedSensor(RotationVector(x, y, z, w))
        val c = controller.camera.value
        assertThat(c.pitchDeg).isWithin(1f).of(30f)
    }

    @Test
    fun `feedSensor in FPS mode rotates but does not translate eye position`() {
        val controller = RoamingController(mode = RoamingMode.Fps, sensitivity = 1.0f, deadbandDeg = 0f)
        val (x, y, z, w) = quaternionFromAxisAngle(axisX = 0f, axisY = 1f, axisZ = 0f, angleDeg = 45f)
        controller.feedSensor(RotationVector(x, y, z, w))
        val c = controller.camera.value
        // FPS does not move eye position from sensor rotation alone
        assertThat(c.eyeX).isEqualTo(0f)
        assertThat(c.eyeZ).isEqualTo(0f)
        // ...but the orientation should still be tracked (via yaw/pitch).
        assertThat(c.yawDeg).isWithin(1f).of(45f)
    }

    @Test
    fun `feedSensor is deadbanded below the configured threshold`() {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 5f)
        // 1° pitch: 2° < 5° deadband threshold → no-op
        val (x, y, z, w) = quaternionFromAxisAngle(axisX = 1f, axisY = 0f, axisZ = 0f, angleDeg = 2f)
        controller.feedSensor(RotationVector(x, y, z, w))
        assertThat(controller.camera.value.pitchDeg).isEqualTo(0f)
    }

    @Test
    fun `feedSensor pitch is clamped to within +-89_5 degrees`() {
        val controller = RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 0f)
        // Force pitch beyond 90° via direct quaternion (close to 180° flip)
        val (x, y, z, w) = quaternionFromAxisAngle(axisX = 1f, axisY = 0f, axisZ = 0f, angleDeg = 179f)
        controller.feedSensor(RotationVector(x, y, z, w))
        assertThat(controller.camera.value.pitchDeg).isLessThan(90f)
        assertThat(controller.camera.value.pitchDeg).isGreaterThan(-90f)
    }
}

// ---------- tiny test helpers (in the same file) ----------

/** axis-angle → unit quaternion (x, y, z, w). */
internal fun quaternionFromAxisAngle(axisX: Float, axisY: Float, axisZ: Float, angleDeg: Float): QuaternionHolder {
    val angleRad = (angleDeg.toDouble() * Math.PI / 180.0).toFloat()
    val half = angleRad / 2f
    val s = kotlin.math.sin(half)
    val c = kotlin.math.cos(half)
    return QuaternionHolder(
        x = axisX * s,
        y = axisY * s,
        z = axisZ * s,
        w = c,
    )
}

internal data class QuaternionHolder(val x: Float, val y: Float, val z: Float, val w: Float)
