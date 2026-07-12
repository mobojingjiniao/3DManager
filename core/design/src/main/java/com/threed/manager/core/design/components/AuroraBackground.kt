package com.threed.manager.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.threed.manager.core.design.theme.AuroraTheme

/**
 * AuroraBackground — full-screen layered backdrop.
 *
 *   1. Solid `Void/Base` fill
 *   2. Aurora radial gradient (green → violet → transparent)
 *   3. Optional `SplatBackgroundView` host layer (set via `ambient`)
 *   4. Optional scrim overlay
 *
 * Every screen wraps its content in this.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0f,
    pointer: Offset? = null,
    content: @Composable () -> Unit,
) {
    val colors = AuroraTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.VoidBase)
            .drawBehind {
                val w = size.width
                val h = size.height
                val cx = pointer?.x ?: w / 2f
                val cy = pointer?.y ?: h * 0.4f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.AuroraGreen.copy(alpha = 0.18f),
                            colors.AuroraViolet.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.8f,
                    ),
                    size = Size(w, h),
                )
                if (scrimAlpha > 0f) {
                    drawRect(color = colors.VoidBase.copy(alpha = scrimAlpha), size = Size(w, h))
                }
            },
    ) {
        content()
    }
}