package com.threed.manager.core.sensor

/**
 * Roaming interaction mode. Determines how drag deltas map to camera motion.
 *
 * - [Orbit]     Camera always looks at a fixed target; drag rotates the camera around it.
 *               Best for inspecting a scene from all sides.
 *
 * - [Fps]       "First person" — drag translates the eye position relative to its current
 *               look direction. Best for "walking through" a scene.
 *
 * - [Trackball] Hybrid — drag rotates the camera (like Orbit) but with a different
 *               damping curve; closest to a 3D modeling tool's viewport.
 */
sealed interface RoamingMode {
    data object Orbit : RoamingMode
    data object Fps : RoamingMode
    data object Trackball : RoamingMode
}
