package com.threed.manager.feature.avatars.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.threed.manager.feature.avatars.capture.CaptureStage

/**
 * Avatar capture flow — Phase 0 stub.
 *
 * Real implementation will:
 *  - Wire CameraX (front/back)
 *  - Capture multi-view frames
 *  - Upload to FormScan for reconstruction
 *  - Poll conversion progress
 *  - Save new Avatar w/ GPS + metadata on success
 */
@Composable
fun AvatarCaptureScreen(onBack: () -> Unit, onClose: () -> Unit) {
    val colors = AuroraTheme.colors
    var stage by remember { mutableStateOf(CaptureStage.IDLE) }
    var frameCount by remember { mutableStateOf(0) }

    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = "Capture avatar", onBack = onClose)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                // Viewfinder placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .clip(RoundedCornerShape(AuroraTheme.radius.l))
                        .background(colors.VoidRaised)
                        .border(1.dp, colors.SurfaceBorderHi, RoundedCornerShape(AuroraTheme.radius.l)),
                ) {
                    // Crosshair / face guide
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(160.dp)
                            .border(
                                width = 2.dp,
                                color = colors.AuroraGreen.copy(alpha = 0.6f),
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = "📷",
                        color = colors.AuroraGreen,
                        style = AuroraTheme.type.displayHero,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    // Top status bar inside viewfinder
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.SignalCoral),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("REC", color = colors.SignalCoral, style = AuroraTheme.type.overline)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Progress
                Text(
                    text = when (stage) {
                        CaptureStage.IDLE -> "READY · 0 / 24 frames"
                        CaptureStage.CAPTURING -> "CAPTURING · $frameCount / 24"
                        CaptureStage.UPLOADING -> "UPLOADING · 62%"
                        CaptureStage.CONVERTING -> "CONVERTING · ETA 2m"
                        CaptureStage.COMPLETE -> "COMPLETE"
                        CaptureStage.FAILED -> "FAILED"
                    },
                    color = colors.TextPrimary,
                    style = AuroraTheme.type.bodyLg.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.SurfaceGlass),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = when (stage) {
                                CaptureStage.IDLE -> 0f
                                CaptureStage.CAPTURING -> (frameCount / 24f)
                                CaptureStage.UPLOADING -> 0.62f
                                CaptureStage.CONVERTING -> 0.85f
                                CaptureStage.COMPLETE -> 1f
                                CaptureStage.FAILED -> 0f
                            })
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.AuroraGreen),
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Tips
                AuroraCard {
                    Text("TIPS", color = colors.AuroraViolet, style = AuroraTheme.type.overline)
                    Spacer(Modifier.height(8.dp))
                    Text("• Slowly orbit around the subject", color = colors.TextSecondary, style = AuroraTheme.type.bodyMd)
                    Text("• Keep 1.5m distance", color = colors.TextSecondary, style = AuroraTheme.type.bodyMd)
                    Text("• Good lighting, steady hands", color = colors.TextSecondary, style = AuroraTheme.type.bodyMd)
                }

                Spacer(Modifier.height(16.dp))

                // Capture button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AuroraButton(
                        text = "Capture frame",
                        onClick = { stage = CaptureStage.CAPTURING; frameCount = (frameCount + 1).coerceAtMost(24) },
                        variant = AuroraButtonVariant.FilledGreen,
                        size = AuroraButtonSize.LG,
                        modifier = Modifier.weight(1f),
                    )
                    AuroraButton(
                        text = "Upload",
                        onClick = { stage = CaptureStage.UPLOADING },
                        variant = AuroraButtonVariant.FilledViolet,
                        size = AuroraButtonSize.LG,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}