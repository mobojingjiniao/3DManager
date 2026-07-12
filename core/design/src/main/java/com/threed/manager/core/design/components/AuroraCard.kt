package com.threed.manager.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.theme.AuroraTheme

/**
 * Glassmorphic surface (L1 / L2 / L3 elevation states).
 *
 * Layer 1 = `SurfaceGlass` fill, 1dp border, signature 28dp radius.
 * Layer 2 = `SurfaceGlassHi` fill (hover/pressed).
 * Layer 3 = `VoidRaised` fill (modal).
 *
 * Pass `accent` to draw a 1.5dp border in that color (for selected/error states).
 */
@Composable
fun AuroraCard(
    modifier: Modifier = Modifier,
    level: CardLevel = CardLevel.L1,
    radius: Dp = AuroraTheme.radius.l,
    accent: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = AuroraTheme.colors
    val shape = RoundedCornerShape(radius)
    val fill = when (level) {
        CardLevel.L1 -> colors.SurfaceGlass
        CardLevel.L2 -> colors.SurfaceGlassHi
        CardLevel.L3 -> colors.VoidRaised
    }
    val borderColor = accent ?: colors.SurfaceBorder
    val borderWidth = if (accent != null) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(borderWidth, borderColor, shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(AuroraTheme.spacing.l),
    ) {
        content()
    }
}

enum class CardLevel { L1, L2, L3 }

/**
 * L4 hero card — gradient surface (green → violet), used for featured elements
 * like the "applied" theme card on the gallery screen.
 */
@Composable
fun AuroraHeroCard(
    modifier: Modifier = Modifier,
    radius: Dp = AuroraTheme.radius.l,
    content: @Composable () -> Unit,
) {
    val colors = AuroraTheme.colors
    val shape = RoundedCornerShape(radius)
    val gradient = Brush.linearGradient(
        colors = listOf(colors.AuroraGreen, colors.AuroraViolet),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient)
            .padding(AuroraTheme.spacing.l),
    ) {
        content()
    }
}