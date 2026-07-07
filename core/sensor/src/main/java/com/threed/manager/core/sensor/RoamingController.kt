package com.threed.manager.core.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Deterministic roaming state machine.
 *
 * Inputs:
 *  - [feedDrag]        : pixel deltas from `MotionEvent` (`dx`, `dy` in screen px)
 *  - [feedSensor]      : (Phase 2.4) raw [RotationVector] from [SensorSource]
 *  - [setMode]         : switch between Orbit / Fps / Trackball
 *
 * Output:
 *  - [camera]          : a hot [StateFlow] of the current [CameraState]
 *
 * Units: 1 drag px = 1 deg, multiplied by [sensitivity] (0.0–2.0,
 * default 1.0). The user's setting `gravitySensitivity` in the
 * preference layer doubles as this sensitivity.
 *
 * Phase 2.3 GREEN: minimum surface for the 8 RED tests. Phase 2.4
 * will add sensor fusion and FPS eye translation in 3D.
 */
class RoamingController(
    mode: RoamingMode,
    private val sensitivity: Float = 1.0f,
    private val deadbandDeg: Float = 0f,
) {

    private val _mode = MutableStateFlow(mode)
    val modeFlow: StateFlow<RoamingMode> = _mode.asStateFlow()

    private val _camera = MutableStateFlow(CameraState.Identity)
    val camera: StateFlow<CameraState> = _camera.asStateFlow()

    /** Maximum pitch magnitude to avoid gimbal lock (gimbal lock itself
     *  is impossible with quaternions, but the visual singularity at
     *  ±90° is real — we clamp slightly inside that range). */
    private val pitchClampDeg = 89.5f

    init {
        require(sensitivity >= 0f) { "sensitivity must be ≥ 0 but was $sensitivity" }
        require(deadbandDeg >= 0f) { "deadbandDeg must be ≥ 0 but was $deadbandDeg" }
    }

    fun setMode(mode: RoamingMode) {
        if (_mode.value == mode) return
        _mode.value = mode
        // Reset the orientation state when switching; FPS and Orbit use
        // different coordinate systems, carrying over would cause a jump.
        _camera.value = CameraState.Identity
    }

    fun feedDrag(dx: Float, dy: Float) {
        val current = _camera.value
        when (_mode.value) {
            RoamingMode.Orbit -> updateOrbit(current, dx, dy)
            RoamingMode.Fps -> updateFps(current, dx, dy)
            RoamingMode.Trackball -> updateTrackball(current, dx, dy)
        }
    }

    /** Phase 2.4: feed device-rotation (gravity-based) to the active mode. */
    fun feedSensor(rotation: RotationVector) {
        // Stub for Phase 2.4; ignored for now so unit tests don't depend on
        // real quaternion math. Implementation will convert the quaternion
        // to yaw/pitch and apply with the same deadband / clamp policy.
    }

    private fun updateOrbit(current: CameraState, dx: Float, dy: Float) {
        val dYaw = applyDeadband(dx)
        val dPitch = applyDeadband(dy)
        val nextYaw = current.yawDeg + dYaw * sensitivity
        val nextPitch = (current.pitchDeg + dPitch * sensitivity)
            .coerceIn(-pitchClampDeg, pitchClampDeg)
        _camera.value = current.copy(yawDeg = nextYaw, pitchDeg = nextPitch)
    }

    private fun updateTrackball(current: CameraState, dx: Float, dy: Float) {
        // Trackball behaves like Orbit but applies the same deadband in both
        // axes. Future versions can add inertia; Phase 2.3 is deterministic.
        updateOrbit(current, dx, dy)
    }

    private fun updateFps(current: CameraState, dx: Float, dy: Float) {
        val dYaw = applyDeadband(dx)
        val dPitch = applyDeadband(dy)
        // FPS translates eye position perpendicular to look direction.
        // dy → forward/backward, dx → strafe. Positive dy drags downward
        // (toward the user), which in FPS convention is "look down", but
        // here we use it for "move forward" to keep semantics consistent
        // with the Orbit pitch sign.
        val dForward = -dPitch * sensitivity * 0.05f
        val dStrafe = dYaw * sensitivity * 0.05f
        val nextEyeX = current.eyeX + dStrafe
        val nextEyeZ = current.eyeZ + dForward
        _camera.value = current.copy(eyeX = nextEyeX, eyeZ = nextEyeZ)
    }

    /** Returns the input unchanged unless it falls inside the deadband. */
    private fun applyDeadband(value: Float): Float =
        if (kotlin.math.abs(value) < deadbandDeg) 0f else value
}
