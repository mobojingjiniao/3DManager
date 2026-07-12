package com.threed.manager.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.theme.AuroraTheme

/**
 * AuroraChip — pill-shaped status / filter chip with 5 tone variants.
 *
 * Tones map to design system signal/aurora colors:
 *   - Green   → CAPTURED, ACTIVE filter, success
 *   - Violet  → IMPORTED, FEATURED, selected filter
 *   - Coral   → FAILED, destructive
 *   - Amber   → SYNCING, warning
 *   - Mint    → READY, success text
 *   - Neutral → SAMPLE, default
 */
@Composable
fun AuroraChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Neutral,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingDot: Boolean = false,
) {
    val colors = AuroraTheme.colors
    val toneColor = when (tone) {
        ChipTone.Green   -> colors.AuroraGreen
        ChipTone.Violet  -> colors.AuroraViolet
        ChipTone.Coral   -> colors.SignalCoral
        ChipTone.Amber   -> colors.SignalAmber
        ChipTone.Mint    -> colors.SignalMint
        ChipTone.Magenta -> colors.AuroraMagenta
        ChipTone.Neutral -> colors.TextPrimary
    }

    val fillColor = if (selected) toneColor else toneColor.copy(alpha = 0.16f)
    val textColor = if (selected) colors.VoidBase else toneColor
    val borderColor = toneColor.copy(alpha = if (selected) 0f else 0.6f)

    val shape = RoundedCornerShape(AuroraTheme.radius.pill)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(fillColor)
            .border(1.dp, borderColor, shape)
            .let { m -> if (onClick != null) m.clickable(onClick = onClick) else m }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        if (leadingDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
        }
        Text(
            text = text.uppercase(),
            style = AuroraTheme.type.overline,
            color = textColor,
        )
    }
}

enum class ChipTone { Green, Violet, Coral, Amber, Mint, Magenta, Neutral }