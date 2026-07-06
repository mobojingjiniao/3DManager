package com.threed.manager.core.system

/**
 * Placeholder for the 4-tier adaptive rendering strategy described in plan §5.
 *
 * Real implementation (HIGH/NORMAL/LOW/STATIC tiers with WebGL2 detection,
 * scene-size budgeting, OEM override map) lands in Phase 4 (#33).
 */
object AdaptiveRenderStrategy {
    enum class Tier { HIGH, NORMAL, LOW, STATIC }
    fun selectTier(): Tier = Tier.HIGH
}
