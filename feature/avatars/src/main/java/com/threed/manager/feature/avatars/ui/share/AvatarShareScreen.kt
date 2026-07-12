package com.threed.manager.feature.avatars.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraButton
import com.threed.manager.core.design.components.AuroraButtonSize
import com.threed.manager.core.design.components.AuroraButtonVariant
import com.threed.manager.core.design.components.AuroraCard
import com.threed.manager.core.design.components.AuroraChip
import com.threed.manager.core.design.components.AuroraTopBar
import com.threed.manager.core.design.components.ChipTone
import com.threed.manager.core.design.theme.AuroraTheme

/** Share sheet — QR + URL + visibility + stats. Phase 0 stub. */
@Composable
fun AvatarShareScreen(avatarId: String, onBack: () -> Unit) {
    val colors = AuroraTheme.colors
    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = "Share avatar", onBack = onBack)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // QR placeholder
                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "QR",
                            color = colors.TextMono,
                            style = AuroraTheme.type.displayTitle,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "porin.app/a/abc123",
                    color = colors.TextPrimary,
                    style = AuroraTheme.type.monoMd,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuroraButton(
                        text = "Copy link",
                        onClick = {},
                        variant = AuroraButtonVariant.Outline,
                        size = AuroraButtonSize.MD,
                        modifier = Modifier.weight(1f),
                    )
                    AuroraButton(
                        text = "System share",
                        onClick = {},
                        variant = AuroraButtonVariant.FilledGreen,
                        size = AuroraButtonSize.MD,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("VISIBILITY", color = colors.TextMono, style = AuroraTheme.type.overline)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraChip(text = "Public", tone = ChipTone.Green, selected = true, leadingDot = true)
                    AuroraChip(text = "Unlisted", tone = ChipTone.Neutral)
                }
            }
        }
    }
}