package com.threed.manager.core.system

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 4.1 — TDD RED tests for [AdaptiveRenderStrategy].
 *
 * The strategy is the single decision point that lets the same app run on
 * a Pixel 3a (≤150k splats → 30 fps LOW) and a Galaxy S24 (≤500k splats
 * → 60 fps HIGH). Phase 4.x wires the resulting tier into the Wallpaper
 * engine and the in-app renderer.
 *
 * Decision inputs:
 *   - sceneMetrics.splatCount
 *   - deviceProfile.tier (Flagship / Mid / Low)
 *   - oemBlocklist   (Huawei EMUI, certain Samsung builds → forced STATIC)
 *   - webgl2Available (false → STATIC, no usable GL path)
 */
class AdaptiveRenderStrategyTest {

    private fun profile(
        tier: DeviceTier = DeviceTier.Mid,
        oemBlocklist: Boolean = false,
        webgl2Available: Boolean = true,
    ) = DeviceProfile(
        tier = tier,
        isOemBlocklisted = oemBlocklist,
        webgl2Available = webgl2Available,
    )

    private fun metrics(splatCount: Long) = SceneMetrics(splatCount = splatCount)

    @Test
    fun `150k splats on flagship yields HIGH tier`() {
        val tier = AdaptiveRenderStrategy.selectTier(metrics(150_000L), profile(tier = DeviceTier.Flagship))
        assertThat(tier).isEqualTo(RenderTier.HIGH)
    }

    @Test
    fun `200k splats on flagship yields NORMAL (boundary at 150k exceeded)`() {
        val tier = AdaptiveRenderStrategy.selectTier(metrics(200_000L), profile(tier = DeviceTier.Flagship))
        assertThat(tier).isEqualTo(RenderTier.NORMAL)
    }

    @Test
    fun `300k splats on mid device yields NORMAL`() {
        val tier = AdaptiveRenderStrategy.selectTier(metrics(300_000L), profile(tier = DeviceTier.Mid))
        assertThat(tier).isEqualTo(RenderTier.NORMAL)
    }

    @Test
    fun `500k splats on low device yields LOW`() {
        val tier = AdaptiveRenderStrategy.selectTier(metrics(500_000L), profile(tier = DeviceTier.Low))
        assertThat(tier).isEqualTo(RenderTier.LOW)
    }

    @Test
    fun `1M splats yields STATIC regardless of device tier`() {
        val tier = AdaptiveRenderStrategy.selectTier(metrics(1_000_000L), profile(tier = DeviceTier.Flagship))
        assertThat(tier).isEqualTo(RenderTier.STATIC)
    }

    @Test
    fun `WebGL2 unavailable forces STATIC even on flagship with small scene`() {
        val tier = AdaptiveRenderStrategy.selectTier(
            metrics(50_000L),
            profile(tier = DeviceTier.Flagship, webgl2Available = false),
        )
        assertThat(tier).isEqualTo(RenderTier.STATIC)
    }

    @Test
    fun `OEM blocklist forces STATIC for all but the smallest scenes`() {
        val tier = AdaptiveRenderStrategy.selectTier(
            metrics(150_000L),
            profile(tier = DeviceTier.Flagship, oemBlocklist = true),
        )
        assertThat(tier).isEqualTo(RenderTier.STATIC)
    }

    @Test
    fun `RenderTier carries fps target and SH degree`() {
        assertThat(RenderTier.HIGH.fps).isEqualTo(60)
        assertThat(RenderTier.HIGH.shDegree).isEqualTo(2)
        assertThat(RenderTier.NORMAL.fps).isEqualTo(30)
        assertThat(RenderTier.NORMAL.shDegree).isEqualTo(1)
        assertThat(RenderTier.LOW.fps).isEqualTo(15)
        assertThat(RenderTier.LOW.shDegree).isEqualTo(0)
        assertThat(RenderTier.STATIC.fps).isEqualTo(0)
    }
}
