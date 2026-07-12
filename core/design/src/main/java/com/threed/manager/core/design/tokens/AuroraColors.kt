package com.threed.manager.core.design.tokens

import androidx.compose.ui.graphics.Color

/**
 * Cinematic Dark Aurora · Color Tokens
 *
 * v3.2 product-grade palette. Single source of truth for every UI surface.
 * No `Color(0xFF…)` literals are allowed outside this file.
 *
 * @see <a href="../../../../../../../../../docs/designs/aurora-v3-realtime/README.md">design system</a>
 */
object AuroraColors {
    // ─── Void ─────────────────────────────────────────────
    val VoidBase   = Color(0xFF06070F)
    val VoidDeep   = Color(0xFF03040A)
    val VoidRaised = Color(0xFF0E1020)

    // ─── Surface (glassmorphic) ───────────────────────────
    val SurfaceGlass   = Color(0x0AFFFFFF) // alpha 0.04
    val SurfaceGlassHi = Color(0x14FFFFFF) // alpha 0.08
    val SurfaceBorder  = Color(0x0FFFFFFF) // alpha 0.06
    val SurfaceBorderHi = Color(0x1FFFFFFF) // alpha 0.12

    // ─── Aurora (signature accents) ──────────────────────
    val AuroraGreen    = Color(0xFF6EFFB7)
    val AuroraGreenDim = Color(0xFF3FCC8A)
    val AuroraViolet   = Color(0xFF9B5CFF)
    val AuroraVioletDim = Color(0xFF6F46CC)
    val AuroraMagenta  = Color(0xFFFF5BD6)

    // ─── Signal ──────────────────────────────────────────
    val SignalCoral = Color(0xFFFF6B6B)
    val SignalAmber = Color(0xFFFFC857)
    val SignalMint  = Color(0xFF7BFFCE)
    val SignalAmberLight = Color(0xFFFFB347) // light leak / accent

    // ─── Text ────────────────────────────────────────────
    val TextPrimary   = Color(0xFFF5F7FF)
    val TextSecondary = Color(0xB8F5F7FF) // 72%
    val TextTertiary  = Color(0x7AF5F7FF) // 48%
    val TextMono      = Color(0xFF9FA8C7)

    // ─── Map (Porin Cloud-style dark map) ────────────────
    val MapBase   = Color(0xFF08111E)
    val MapLand   = Color(0xFF1A2438)
    val MapGrid   = Color(0x0DF4EDE4) // 5% white
    val MapWater  = Color(0xFF0A1428)

    // ─── Scrim ───────────────────────────────────────────
    val Scrim = Color(0xB806070F) // 72% black
}