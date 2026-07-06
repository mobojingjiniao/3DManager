package com.threed.manager.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material Design (M2) theme scaffold.
 *
 * Phase 4 will replace this with the full ThemePack system
 * (Material 3 dynamic color + custom theme packs). M2 lets us ship a
 * working Compose UI in Phase 0 without pulling material3 artifacts.
 */
private val FallbackLight = lightColors(
    primary = Color(0xFF1A73E8),
    primaryVariant = Color(0xFF1666D3),
    secondary = Color(0xFF34C759),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFAFAFA),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF101112),
    onSurface = Color(0xFF101112),
)
private val FallbackDark = darkColors(
    primary = Color(0xFF8AB4F8),
    primaryVariant = Color(0xFF669DF6),
    secondary = Color(0xFF81C995),
    background = Color(0xFF101112),
    surface = Color(0xFF1A1C1E),
    onPrimary = Color(0xFF101112),
    onSecondary = Color(0xFF101112),
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
)

@Composable
fun Theme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (useDarkTheme) FallbackDark else FallbackLight,
        content = content,
    )
}
