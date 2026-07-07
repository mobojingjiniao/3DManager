package com.threed.manager.core.sensor

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 2.1 — TDD RED tests for [GravityFilter].
 *
 * Filter is a 1-D low-pass applied component-wise to a [GravityVector]:
 *     y[n] = α * x[n] + (1 - α) * y[n-1]
 *     y[0] = x[0]  (first sample is the seed)
 *
 * RED at the moment of writing: `GravityFilter` and `GravityVector` not yet
 * declared. Once we add the minimum stubs in Phase 2.1 GREEN, the assertions
 * below all pass.
 */
class GravityFilterTest {

    @Test
    fun `first sample passes through unchanged`() {
        val filter = GravityFilter(alpha = 0.8f)
        val out = filter.process(GravityVector(0f, 0f, -9.81f))
        assertThat(out.x).isEqualTo(0f)
        assertThat(out.y).isEqualTo(0f)
        assertThat(out.z).isEqualTo(-9.81f)
    }

    @Test
    fun `alpha 0p5 produces exponential smoothing`() {
        val filter = GravityFilter(alpha = 0.5f)
        filter.process(GravityVector(10f, 0f, 0f))
        val out = filter.process(GravityVector(0f, 0f, -9.81f))
        // α * new + (1-α) * prev = 0.5*0 + 0.5*10 = 5  on x
        //                            0.5*0 + 0.5*0  = 0  on y
        //                            0.5*(-9.81) + 0.5*0 = -4.905 on z
        assertThat(out.x).isWithin(1e-4f).of(5f)
        assertThat(out.y).isWithin(1e-4f).of(0f)
        assertThat(out.z).isWithin(1e-4f).of(-4.905f)
    }

    @Test
    fun `alpha 0p8 converges faster to new value`() {
        val filter = GravityFilter(alpha = 0.8f)
        filter.process(GravityVector(0f, 0f, -9.81f))  // seed
        // 5 successive samples of (0, 0, 0) — should drive z toward 0
        var out: GravityVector = GravityVector(0f, 0f, -9.81f)
        repeat(5) { out = filter.process(GravityVector(0f, 0f, 0f)) }
        // After 5 iters: z = (0.2)^5 * (-9.81) ≈ -0.0031
        assertThat(out.z).isWithin(0.01f).of(-0.0031f)
    }

    @Test
    fun `alpha 0 freezes output to seed value`() {
        val filter = GravityFilter(alpha = 0f)
        filter.process(GravityVector(0f, 0f, -9.81f))  // seed
        val out = filter.process(GravityVector(50f, 0f, 0f))  // shouldn't move
        assertThat(out).isEqualTo(GravityVector(0f, 0f, -9.81f))
    }

    @Test
    fun `alpha 1 produces passthrough`() {
        val filter = GravityFilter(alpha = 1f)
        filter.process(GravityVector(0f, 0f, -9.81f))  // seed
        val out = filter.process(GravityVector(3f, 4f, 5f))
        assertThat(out).isEqualTo(GravityVector(3f, 4f, 5f))
    }

    @Test
    fun `magnitude is approximately preserved when input is stable`() {
        // Simulate a 3-axis device tilted so that the gravity vector in
        // device coordinates is (0, 0, -9.81). The output magnitude should
        // stay near 9.81 once the filter has converged.
        val filter = GravityFilter(alpha = 0.5f)
        var out = GravityVector(0f, 0f, -9.81f)
        repeat(20) { out = filter.process(GravityVector(0f, 0f, -9.81f)) }
        val magnitude = kotlin.math.sqrt(out.x * out.x + out.y * out.y + out.z * out.z)
        assertThat(magnitude).isWithin(0.05f).of(9.81f)
    }

    @Test
    fun `alpha out of range throws IllegalArgumentException`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            GravityFilter(alpha = -0.1f)
        }
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            GravityFilter(alpha = 1.1f)
        }
    }
}
