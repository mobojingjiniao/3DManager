package com.threed.manager.feature.avatars.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraBottomBar
import com.threed.manager.core.design.components.AuroraCard
import com.threed.manager.core.design.components.AuroraChip
import com.threed.manager.core.design.components.AuroraFloatingAction
import com.threed.manager.core.design.components.AuroraTopBar
import com.threed.manager.core.design.components.ChipTone
import com.threed.manager.core.design.theme.AuroraTheme
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId

@Composable
fun AvatarLibraryScreen(
    onOpenAvatar: (AvatarId) -> Unit,
    onOpenMap: () -> Unit,
    onCapture: () -> Unit,
    onTabNavigate: (String) -> Unit = {},
    vm: AvatarLibraryViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = AuroraTheme.colors

    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // System status bar (simple)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text("9:41", color = colors.TextPrimary, style = AuroraTheme.type.monoMd)
                Text(
                    "●●●●○ 87%",
                    color = colors.TextPrimary,
                    style = AuroraTheme.type.monoMd,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }

            AuroraTopBar(title = "Avatars")

            Text(
                text = "${state.avatars.size} AVATARS · ${state.avatars.count { it.source.name == "CAPTURED" }} CAPTURED · ${state.avatars.count { it.source.name == "IMPORTED" }} IMPORTED",
                color = colors.TextMono,
                style = AuroraTheme.type.monoSm,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))

            // Filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AuroraFilterChip(
                    label = "All",
                    selected = state.filter == AvatarFilter.ALL,
                    onClick = { vm.setFilter(AvatarFilter.ALL) },
                )
                AuroraFilterChip(
                    label = "Captured",
                    selected = state.filter == AvatarFilter.CAPTURED,
                    onClick = { vm.setFilter(AvatarFilter.CAPTURED) },
                )
                AuroraFilterChip(
                    label = "Imported",
                    selected = state.filter == AvatarFilter.IMPORTED,
                    onClick = { vm.setFilter(AvatarFilter.IMPORTED) },
                )
                AuroraFilterChip(
                    label = "★",
                    selected = state.filter == AvatarFilter.FAVORITES,
                    onClick = { vm.setFilter(AvatarFilter.FAVORITES) },
                )
                Spacer(Modifier.weight(1f))
                AuroraFilterChip(
                    label = "Map",
                    selected = false,
                    onClick = onOpenMap,
                    accent = colors.AuroraViolet,
                )
            }
            Spacer(Modifier.height(12.dp))

            if (state.visible.isEmpty() && state.avatars.isNotEmpty()) {
                EmptyFilterState(filter = state.filter)
            } else if (state.avatars.isEmpty()) {
                EmptyLibraryState(onCapture = onCapture, onMap = onOpenMap)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(state.visible, key = { it.id.raw }) { avatar ->
                        AvatarGridCell(
                            avatar = avatar,
                            onClick = { onOpenAvatar(avatar.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AuroraBottomBar(
                currentRoute = "avatars",
                onNavigate = onTabNavigate,
                fab = {
                    AuroraFloatingAction(onClick = onCapture)
                },
            )
        }
    }
}

@Composable
private fun AuroraFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: Color = AuroraTheme.colors.AuroraGreen,
) {
    val colors = AuroraTheme.colors
    val shape = RoundedCornerShape(AuroraTheme.radius.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent else colors.SurfaceGlass)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = if (selected) colors.VoidBase else colors.TextPrimary,
            style = AuroraTheme.type.caption.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun AvatarGridCell(
    avatar: Avatar,
    onClick: () -> Unit,
) {
    val colors = AuroraTheme.colors
    val accent = when (avatar.source.name) {
        "CAPTURED" -> colors.AuroraGreen
        "IMPORTED" -> colors.AuroraViolet
        else       -> colors.AuroraMagenta
    }
    AuroraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accent = if (avatar.favorite) colors.AuroraGreen else null,
    ) {
        Column {
            // Thumbnail (gradient placeholder w/ mini particles)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.VoidRaised),
            ) {
                // Mini particle silhouette
                MiniSilhouette(seed = avatar.id.raw.hashCode())
                // Status pill
                AuroraChip(
                    text = avatar.source.name,
                    tone = if (avatar.source.name == "CAPTURED") ChipTone.Green else ChipTone.Violet,
                    leadingDot = true,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                )
                // Mini location pin (top right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.VoidDeep.copy(alpha = 0.85f))
                        .border(1.dp, accent.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📍", color = accent, style = AuroraTheme.type.caption)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = avatar.name,
                color = colors.TextPrimary,
                style = AuroraTheme.type.bodyLg.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = avatar.metadata.locationName ?: "—",
                    color = colors.TextSecondary,
                    style = AuroraTheme.type.bodySm,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${humanSplatCount(avatar.metadata.splatCount)} · ${humanBytes(avatar.metadata.fileBytes)}",
                color = colors.TextMono,
                style = AuroraTheme.type.monoSm,
            )
        }
    }
}

@Composable
private fun MiniSilhouette(seed: Int) {
    val colors = AuroraTheme.colors
    Box(modifier = Modifier.fillMaxSize()) {
        // Random particle scatter (deterministic by seed)
        val rnd = java.util.Random(seed.toLong())
        repeat(28) {
            val x = rnd.nextFloat()
            val y = rnd.nextFloat() * 0.9f + 0.05f
            val r = 1.5f + rnd.nextFloat() * 2.5f
            val c = if (rnd.nextFloat() < 0.5f) colors.AuroraGreen else colors.AuroraViolet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = (x * 100).dp, top = (y * 100).dp)
                    .size(r.dp)
                    .clip(CircleShape)
                    .background(c.copy(alpha = 0.7f)),
            )
        }
    }
}

@Composable
private fun EmptyFilterState(filter: AvatarFilter) {
    val colors = AuroraTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No avatars in this view",
                color = colors.TextPrimary,
                style = AuroraTheme.type.displaySection,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try a different filter",
                color = colors.TextSecondary,
                style = AuroraTheme.type.bodyMd,
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(onCapture: () -> Unit, onMap: () -> Unit) {
    val colors = AuroraTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No avatars yet",
                color = colors.TextPrimary,
                style = AuroraTheme.type.displaySection,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Capture your first avatar with the camera,\nor import a .ksplat file from your device.",
                color = colors.TextSecondary,
                style = AuroraTheme.type.bodyMd,
            )
            Spacer(Modifier.height(24.dp))
            com.threed.manager.core.design.components.AuroraButton(
                text = "Capture avatar",
                onClick = onCapture,
                variant = com.threed.manager.core.design.components.AuroraButtonVariant.FilledGreen,
                size = com.threed.manager.core.design.components.AuroraButtonSize.LG,
            )
            Spacer(Modifier.height(12.dp))
            com.threed.manager.core.design.components.AuroraButton(
                text = "Open map",
                onClick = onMap,
                variant = com.threed.manager.core.design.components.AuroraButtonVariant.OutlineViolet,
                size = com.threed.manager.core.design.components.AuroraButtonSize.MD,
            )
        }
    }
}

internal fun humanSplatCount(n: Long): String =
    if (n >= 1_000_000) "%.1fM splats".format(n / 1_000_000.0) else "%.0fK splats".format(n / 1_000.0)

internal fun humanBytes(b: Long): String {
    val kb = b / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}

// Import here so Modifier.border is in scope (top of file needs it for composables).
// (Helper removed; using androidx.compose.foundation.border directly.)