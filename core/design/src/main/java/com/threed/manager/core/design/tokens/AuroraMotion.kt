package com.threed.manager.core.design.tokens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Cinematic Dark Aurora · Motion tokens.
 *
 * Easings + durations + springs, named for reuse.
 */
object AuroraMotion {
    // Easings
    val Standard   = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // cinematic
    val Exit       = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)

    // Durations (ms) — plain Int for use with tween / spring APIs.
    const val InstantMs    = 80
    const val FastMs       = 180
    const val MediumMs     = 280
    const val SlowMs       = 480
    const val BreathMs     = 3000
    const val AuroraDriftMs = 8000

    // Animation specs (use directly with `animate*AsState`)
    val InstantSpec: AnimationSpec<Float> = tween(durationMillis = InstantMs, easing = Standard)
    val FastSpec: AnimationSpec<Float>    = tween(durationMillis = FastMs, easing = Standard)
    val MediumSpec: AnimationSpec<Float>  = tween(durationMillis = MediumMs, easing = Standard)
    val EmphasizedSpec: AnimationSpec<Float> = tween(durationMillis = SlowMs, easing = Emphasized)
    val SlowSpec: AnimationSpec<Float>    = tween(durationMillis = 800, easing = Emphasized)
    val BreathSpec: AnimationSpec<Float>  = tween(durationMillis = BreathMs, easing = Standard)
    val AuroraDriftSpec: AnimationSpec<Float> = tween(durationMillis = AuroraDriftMs, easing = Standard)

    // Spring specs
    val SnapSpring: AnimationSpec<Float> = spring(stiffness = 320f, dampingRatio = 0.65f)
    val SoftSpring: AnimationSpec<Float> = spring(stiffness = 180f, dampingRatio = 0.85f)
}