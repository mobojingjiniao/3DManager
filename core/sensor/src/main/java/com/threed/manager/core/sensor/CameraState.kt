package com.threed.manager.core.sensor

/**
 * Concrete camera transform produced by [RoamingController].
 *
 * Coordinates are in world space, with the scene origin at `(0, 0, 0)`.
 * Yaw is rotation around the world Y axis; pitch is rotation around the
 * local X axis (after yaw). The orientation is then applied to a default
 * `eye` (typically `(0, 0, radius)`) to produce a final eye position.
 *
 * Phase 2.3 only tracks the orientation. Phase 2.4 will also expose an
 * explicit `eye` position for FPS / cinematic modes.
 */
data class CameraState(
    val yawDeg: Float = 0f,
    val pitchDeg: Float = 0f,
    val eyeX: Float = 0f,
    val eyeY: Float = 0f,
    val eyeZ: Float = 0f,
) {
    companion object {
        val Identity = CameraState()
    }
}
