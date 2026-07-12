package com.threed.manager.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.threed.manager.core.design.tokens.AuroraColors
import com.threed.manager.core.design.tokens.AuroraMotion
import com.threed.manager.core.design.tokens.AuroraRadius
import com.threed.manager.core.design.tokens.AuroraSpacing
import com.threed.manager.core.design.tokens.AuroraType

/**
 * Cinematic Dark Aurora · Main theme.
 *
 * Replaces the Phase 0 M2 fallback with a full Aurora token surface.
 * Always dark — light mode is reserved for accessibility / OEM modes.
 */
val LocalAuroraColors  = staticCompositionLocalOf { AuroraColors }
val LocalAuroraSpacing = staticCompositionLocalOf { AuroraSpacing }
val LocalAuroraRadius  = staticCompositionLocalOf { AuroraRadius }
val LocalAuroraMotion  = staticCompositionLocalOf { AuroraMotion }
val LocalAuroraType    = staticCompositionLocalOf { AuroraType }

// ─── Material ColorScheme mappings ──────────────────────────────
private val AuroraDarkMaterial = darkColors(
    primary       = AuroraColors.AuroraGreen,
    primaryVariant = AuroraColors.AuroraGreenDim,
    secondary     = AuroraColors.AuroraViolet,
    secondaryVariant = AuroraColors.AuroraVioletDim,
    background    = AuroraColors.VoidBase,
    surface       = AuroraColors.VoidRaised,
    onPrimary     = AuroraColors.VoidBase,
    onSecondary   = AuroraColors.VoidBase,
    onBackground  = AuroraColors.TextPrimary,
    onSurface     = AuroraColors.TextPrimary,
    error         = AuroraColors.SignalCoral,
    onError       = AuroraColors.VoidBase,
)

private val AuroraLightMaterial = lightColors(
    primary       = AuroraColors.AuroraGreen,
    primaryVariant = AuroraColors.AuroraGreenDim,
    secondary     = AuroraColors.AuroraViolet,
    background    = Color(0xFFFAFAFA),
    surface       = Color.White,
    onPrimary     = Color.Black,
    onSecondary   = Color.Black,
    onBackground  = AuroraColors.VoidBase,
    onSurface     = AuroraColors.VoidBase,
)

@Composable
fun AuroraTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (useDarkTheme) AuroraDarkMaterial else AuroraLightMaterial
    CompositionLocalProvider(
        LocalAuroraColors  provides AuroraColors,
        LocalAuroraSpacing provides AuroraSpacing,
        LocalAuroraRadius  provides AuroraRadius,
        LocalAuroraMotion  provides AuroraMotion,
        LocalAuroraType    provides AuroraType,
    ) {
        MaterialTheme(
            colors = scheme,
            typography = AuroraType.toMaterialTypography(),
            content = content,
        )
    }
}

/** Shorthand accessor for tokens inside composables. */
object AuroraTheme {
    val colors: AuroraColors
        @Composable @ReadOnlyComposable
        get() = LocalAuroraColors.current
    val spacing: AuroraSpacing
        @Composable @ReadOnlyComposable
        get() = LocalAuroraSpacing.current
    val radius: AuroraRadius
        @Composable @ReadOnlyComposable
        get() = LocalAuroraRadius.current
    val motion: AuroraMotion
        @Composable @ReadOnlyComposable
        get() = LocalAuroraMotion.current
    val type: AuroraType
        @Composable @ReadOnlyComposable
        get() = LocalAuroraType.current
}