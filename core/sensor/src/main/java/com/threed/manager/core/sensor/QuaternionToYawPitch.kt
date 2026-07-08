package com.threed.manager.core.sensor

import kotlin.math.atan2
import kotlin.math.asin

/**
 * Convert a unit quaternion (q = w + xi + yj + zk) to yaw / pitch angles
 * in degrees, following the **Y-up** Tait-Bryan ZYX convention used by
 * Filament / three.js. Roll is intentionally not exposed because the
 * camera rig does not need it.
 *
 * Derivation: starting from a unit quaternion, the rotation matrix R is
 * applied as R = Y(yaw) · X(pitch) · Z(roll). Inverting gives:
 *
 *   pitch = asin( 2(wx − yz) )
 *   yaw   = atan2( 2(wy + xz),  1 − 2(x² + z²) )
 *   roll  = atan2( 2(wz + xy),  1 − 2(x² + y²) )
 *
 * For a pure X rotation q = (sin(θ/2), 0, 0, cos(θ/2)) this yields
 * pitch = θ, yaw = 0 — which matches the test's expectation. A naive
 * "Z-up" version of this formula would give yaw = pitch = 0 for a pure
 * X rotation, which is wrong for a Y-up camera.
 */
fun quaternionToYawPitch(q: RotationVector): Pair<Float, Float> {
    val x = q.x
    val y = q.y
    val z = q.z
    val w = q.w
    val sinp = (2.0 * (w * x - y * z)).coerceIn(-1.0, 1.0)
    val pitchRad = asin(sinp)
    val yawRad = atan2(
        2.0 * (w * y + x * z),
        1.0 - 2.0 * (y * y + z * z),
    )
    val pitchDeg = Math.toDegrees(pitchRad).toFloat()
    val yawDeg = Math.toDegrees(yawRad).toFloat()
    return yawDeg to pitchDeg
}
