package com.threed.manager.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.theme.AuroraTheme

/** Button height scale. */
enum class AuroraButtonSize(val height: Dp, val horizontalPad: Dp, val cornerRadius: Dp) {
    SM(40.dp, 16.dp, 20.dp),
    MD(56.dp, 24.dp, 28.dp),
    LG(72.dp, 32.dp, 36.dp),
}

enum class AuroraButtonVariant { FilledGreen, FilledViolet, FilledGradient, Tonal, Outline, OutlineViolet, Text, Destructive }

/**
 * AuroraButton — variants:
 *   FilledGreen / FilledViolet — solid color
 *   FilledGradient — green → violet diagonal
 *   Tonal — translucent surface
 *   Outline / OutlineViolet — outlined only
 *   Text — text-only
 *   Destructive — coral
 */
@Composable
fun AuroraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuroraButtonVariant = AuroraButtonVariant.FilledGreen,
    size: AuroraButtonSize = AuroraButtonSize.MD,
    leadingGlyph: String? = null,
    enabled: Boolean = true,
) {
    val colors = AuroraTheme.colors

    val (bg, fg) = when (variant) {
        AuroraButtonVariant.FilledGreen   -> colors.AuroraGreen to colors.VoidBase
        AuroraButtonVariant.FilledViolet  -> colors.AuroraViolet to colors.VoidBase
        AuroraButtonVariant.FilledGradient -> null to colors.VoidBase
        AuroraButtonVariant.Tonal         -> colors.SurfaceGlassHi to colors.TextPrimary
        AuroraButtonVariant.Outline       -> null to colors.TextPrimary
        AuroraButtonVariant.OutlineViolet -> null to colors.AuroraViolet
        AuroraButtonVariant.Text          -> null to colors.AuroraGreen
        AuroraButtonVariant.Destructive   -> colors.SignalCoral to colors.VoidBase
    }
    val alpha = if (enabled) 1f else 0.4f

    val brush: Brush? = when (variant) {
        AuroraButtonVariant.FilledGradient ->
            Brush.linearGradient(listOf(colors.AuroraGreen, colors.AuroraViolet))
        else -> null
    }
    val borderColor = when (variant) {
        AuroraButtonVariant.Outline       -> colors.SurfaceBorderHi
        AuroraButtonVariant.OutlineViolet -> colors.AuroraViolet.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val shape = RoundedCornerShape(size.cornerRadius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .let { m ->
                if (brush != null) m.background(brush)
                else if (bg != null) m.background(bg)
                else m
            }
            .border(if (variant == AuroraButtonVariant.Outline || variant == AuroraButtonVariant.OutlineViolet) 1.5.dp else 0.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = size.horizontalPad, vertical = (size.height - 32.dp) / 2),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingGlyph != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(fg.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(leadingGlyph, color = fg, style = AuroraTheme.type.caption)
                }
            }
            Text(
                text = text,
                color = fg.copy(alpha = alpha),
                style = AuroraTheme.type.bodyMd.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            )
        }
    }
}