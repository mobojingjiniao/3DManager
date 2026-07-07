package com.threed.manager.core.sensor

/**
 * A 3-axis gravity reading in device frame.
 *
 * Units: m/s² (raw Sensor.TYPE_GRAVITY output). At rest, |g| ≈ 9.81.
 *
 * The 3 axes are device-local:
 *  - x: right
 *  - y: up (along the device's screen)
 *  - z: out of the screen toward the user
 */
data class GravityVector(val x: Float, val y: Float, val z: Float) {
    val magnitude: Float
        get() = kotlin.math.sqrt(x * x + y * y + z * z)

    companion object {
        val Zero = GravityVector(0f, 0f, 0f)
    }
}
