package com.threed.manager.core.system

/**
 * Decision function: pick a [RenderTier] from scene + device facts.
 *
 * Algorithm (Phase 4.1):
 *  1. WebGL2 unavailable → STATIC. (No live render path can succeed.)
 *  2. OEM blocklist and scene > 50k splats → STATIC.
 *     (Huawei EMUI / some Samsung builds throttle background GPU.)
 *  3. Otherwise classify by scene size and device tier.
 *
 * Tier | Flagship (GPU flagship, ≥ 8 GB RAM)         | Mid-range         | Low-end
 * ----+----------------------------------------------+-------------------+------------
 * ≤150k | HIGH (60 fps, SH 2)                         | HIGH              | NORMAL
 * 150k–300k | NORMAL (30 fps, SH 1)                  | NORMAL            | LOW
 * 300k–500k | NORMAL                                    | NORMAL            | LOW
 * > 500k | STATIC                                     | STATIC            | STATIC
 *
 * The exact thresholds are documented in plan v2 §5 and may be tightened
 * as real-device profiling lands in Phase 5.
 */
object AdaptiveRenderStrategy {

    private const val HIGH_SPLAT_CAP = 150_000L
    private const val NORMAL_SPLAT_CAP = 300_000L
    private const val LOW_SPLAT_CAP = 500_000L
    private const val OEM_BLOCKLIST_SAFE_SPLATS = 50_000L

    fun selectTier(scene: SceneMetrics, device: DeviceProfile): RenderTier {
        // Hard gates first.
        if (!device.webgl2Available) return RenderTier.STATIC
        if (device.isOemBlocklisted && scene.splatCount > OEM_BLOCKLIST_SAFE_SPLATS) {
            return RenderTier.STATIC
        }

        // Soft gates.
        return when (device.tier) {
            DeviceTier.Flagship -> when {
                scene.splatCount <= HIGH_SPLAT_CAP -> RenderTier.HIGH
                scene.splatCount <= LOW_SPLAT_CAP -> RenderTier.NORMAL
                else -> RenderTier.STATIC
            }
            DeviceTier.Mid -> when {
                scene.splatCount <= HIGH_SPLAT_CAP -> RenderTier.HIGH
                scene.splatCount <= LOW_SPLAT_CAP -> RenderTier.NORMAL
                else -> RenderTier.STATIC
            }
            DeviceTier.Low -> when {
                scene.splatCount <= NORMAL_SPLAT_CAP -> RenderTier.NORMAL
                scene.splatCount <= LOW_SPLAT_CAP -> RenderTier.LOW
                else -> RenderTier.STATIC
            }
        }
    }
}
