package com.threed.manager.core.sensor

/**
 * Quaternion-style device orientation.
 *
 * Source: Sensor.TYPE_GAME_ROTATION_VECTOR
 *
 * Components (x, y, z, w):
 *  - Rotation from device frame to world frame.
 *  - Unit quaternion (|q| = 1).
 *  - Identity quaternion (0, 0, 0, 1) means device is in its default
 *    orientation (portrait, screen up).
 *
 * Using quaternions (not Euler angles) avoids gimbal lock and is the
 * format that Filament / three.js camera math expect.
 */
data class RotationVector(val x: Float, val y: Float, val z: Float, val w: Float) {
    companion object {
        val Identity = RotationVector(0f, 0f, 0f, 1f)
    }
}
