package com.threed.manager.feature.avatars.ui.wallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraBottomBar
import com.threed.manager.core.design.components.AuroraButton
import com.threed.manager.core.design.components.AuroraButtonSize
import com.threed.manager.core.design.components.AuroraButtonVariant
import com.threed.manager.core.design.components.AuroraCard
import com.threed.manager.core.design.components.AuroraChip
import com.threed.manager.core.design.components.AuroraFloatingAction
import com.threed.manager.core.design.components.AuroraTopBar
import kotlinx.coroutines.launch
import com.threed.manager.core.design.components.ChipTone
import com.threed.manager.core.design.theme.AuroraTheme

/** Wallpaper preview + set-as-wallpaper flow. */
@Composable
fun WallpaperScreen(onTabNavigate: (String) -> Unit = {}) {
    val colors = AuroraTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val applier = remember { com.threed.manager.feature.avatars.wallpaper.WallpaperApplier(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val isLive = remember { applier.isLiveWallpaperActive }

    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = "Wallpaper")
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(AuroraTheme.radius.l))
                        .background(colors.VoidRaised),
                ) {
                    Text("LIVE PREVIEW", color = colors.TextMono, style = AuroraTheme.type.overline, modifier = Modifier.align(Alignment.Center))
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.VoidDeep.copy(alpha = 0.85f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text("FPS 30 · LIVE", color = colors.AuroraGreen, style = AuroraTheme.type.monoSm)
                    }
                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.AuroraGreen)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text("ACTIVE", color = colors.VoidBase, style = AuroraTheme.type.overline)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Active avatar: Me (Idle)", color = colors.TextPrimary, style = AuroraTheme.type.bodyLg.copy(fontWeight = FontWeight.SemiBold))
                Text("Tier: HIGH · 30fps · 1.2M splats · sensor-driven", color = colors.TextMono, style = AuroraTheme.type.monoSm)
                Spacer(Modifier.height(16.dp))
                AuroraChip(text = "HIGH · 60 FPS · TIER", tone = ChipTone.Green, leadingDot = true)
                Spacer(Modifier.height(24.dp))
                AuroraButton(
                    text = "Set as live wallpaper",
                    onClick = {
                        val intent = applier.launchLiveWallpaperPicker()
                        context.startActivity(intent)
                    },
                    variant = AuroraButtonVariant.FilledGradient,
                    size = AuroraButtonSize.LG,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    AuroraButton(
                        text = "Lock screen",
                        onClick = {
                            val av = com.threed.manager.feature.avatars.AvatarRepositoryProvider.repository.avatars.value
                                .firstOrNull { it.favorite }
                                ?: com.threed.manager.feature.avatars.AvatarRepositoryProvider.repository.avatars.value.firstOrNull()
                            av?.let { scope.launch { applier.applyStaticBitmap(it) } }
                        },
                        variant = AuroraButtonVariant.Outline,
                        size = AuroraButtonSize.MD,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    AuroraButton(
                        text = "Set on both",
                        onClick = {
                            val av = com.threed.manager.feature.avatars.AvatarRepositoryProvider.repository.avatars.value.firstOrNull()
                            av?.let { scope.launch { applier.applyStaticBitmap(it) } }
                        },
                        variant = AuroraButtonVariant.OutlineViolet,
                        size = AuroraButtonSize.MD,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.weight(1f))
                AuroraBottomBar(
                    currentRoute = "wallpaper",
                    onNavigate = onTabNavigate,
                    fab = { AuroraFloatingAction(onClick = {}) },
                )
            }
        }
    }
}

@Composable
private fun Spacer(modifier: Modifier) = androidx.compose.foundation.layout.Spacer(modifier)