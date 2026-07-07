package com.threed.manager.core.sensor

import kotlin.math.sqrt

/**
 * One-dimensional exponential low-pass filter applied component-wise
 * to a sequence of [GravityVector] samples.
 *
 * Algorithm (per axis):
 *     y[n] = α * x[n] + (1 - α) * y[n-1]
 *     y[0] = x[0]                          (first sample is the seed)
 *
 * @param α  Smoothing factor in `[0, 1]`. `0` freezes output at seed,
 *           `1` is a pure passthrough. Defaults to `0.5` (Nyquist-stable
 *           for a 50 Hz sensor at 9.81 m/s² jitter).
 *
 * Phase 2.1 GREEN: minimum surface for the 7 RED tests in
 * [GravityFilterTest]. Phase 2.2 will add a stateful sibling
 * [GravityVector] accumulator and Flow-based [SensorSource] integration.
 */
class GravityFilter(private val alpha: Float) {

    init {
        require(alpha in 0f..1f) { "alpha must be in [0, 1] but was $alpha" }
    }

    private var last: GravityVector? = null

    /**
     * Feed the next raw sample and return the filtered output.
     *
     * Thread-safety: NOT safe for concurrent calls. Wrap in an external
     * `Mutex` or `Channel(Channel.CONFLATED)` if used off the main thread.
     */
    fun process(sample: GravityVector): GravityVector {
        val prev = last
        val out = if (prev == null) {
            sample
        } else {
            val a = alpha
            GravityVector(
                x = a * sample.x + (1f - a) * prev.x,
                y = a * sample.y + (1f - a) * prev.y,
                z = a * sample.z + (1f - a) * prev.z,
            )
        }
        last = out
        return out
    }

    /**
     * Forget the running state. Useful when the controller is paused or
     * when the activity goes background → foreground (avoid filter
     * carrying the stale seed across a context switch).
     */
    fun reset() {
        last = null
    }

    companion object {
        /**
         * Sanity-check a sequence for 1-axis filter stability:
         * `|out - in| ≤ |in| * α / (1 - α)` after convergence.
         * Documented only; not used in the production path.
         */
        @Suppress("unused")
        fun maxSteadyStateError(alpha: Float, inputAmplitude: Float): Float =
            if (alpha >= 1f) 0f else inputAmplitude * alpha / (1f - alpha)

        /** Default α for a typical 50 Hz accelerometer stream. */
        const val DEFAULT_ALPHA: Float = 0.5f

        /** Helper to compute Euclidean magnitude (used by tests + by callers
         *  that need to confirm gravity-direction preservation). */
        fun magnitude(v: GravityVector): Float = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
    }
}
