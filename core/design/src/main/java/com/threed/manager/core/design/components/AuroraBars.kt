package com.threed.manager.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.theme.AuroraTheme

/**
 * AuroraTopBar — translucent top app bar with back arrow + title + actions.
 *
 * Used at the top of every screen below the system status bar.
 */
@Composable
fun AuroraTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = AuroraTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = AuroraTheme.spacing.l),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("←", style = AuroraTheme.type.displaySection, color = colors.TextPrimary)
            }
            Spacer(Modifier.width(AuroraTheme.spacing.m))
        }
        Text(
            text = title,
            style = AuroraTheme.type.displayTitle,
            color = colors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/**
 * AuroraBottomBar — fixed 4-tab bottom navigation + center FAB slot.
 *
 * Tabs: Avatars / Map / Wallpaper / Settings.
 * FAB is rendered ON TOP of the bar (anchored).
 */
@Composable
fun AuroraBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    fab: @Composable () -> Unit = {},
) {
    val colors = AuroraTheme.colors
    val tabs = listOf(
        BottomTab("avatars", "Avatars", GlyphKind.Avatars),
        BottomTab("map", "Map", GlyphKind.Map),
        BottomTab("wallpaper", "Wallpaper", GlyphKind.Wallpaper),
        BottomTab("settings", "Settings", GlyphKind.Settings),
    )
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(colors.VoidDeep)
                .align(Alignment.BottomCenter),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.SurfaceBorder)
                .align(Alignment.TopCenter),
        )
        Box(modifier = Modifier.align(Alignment.TopCenter)) { fab() }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { idx, tab ->
                val isActive = currentRoute == tab.route
                val isFabSlot = idx == 1
                if (isFabSlot) {
                    Spacer(Modifier.width(112.dp))
                }
                BottomTabCell(
                    label = tab.label,
                    kind = tab.kind,
                    active = isActive,
                    onClick = { onNavigate(tab.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class BottomTab(val route: String, val label: String, val kind: GlyphKind)

private enum class GlyphKind { Avatars, Map, Wallpaper, Settings }

@Composable
private fun BottomTabCell(
    label: String,
    kind: GlyphKind,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuroraTheme.colors
    val accent = colors.AuroraGreen
    val fg = if (active) accent else colors.TextPrimary.copy(alpha = 0.55f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) accent.copy(alpha = 0.12f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when (kind) {
                    GlyphKind.Avatars    -> "◆"
                    GlyphKind.Map        -> "▣"
                    GlyphKind.Wallpaper  -> "▦"
                    GlyphKind.Settings   -> "⚙"
                },
                style = AuroraTheme.type.bodyLg,
                color = fg,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = AuroraTheme.type.caption.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
        )
    }
}

/** Center FAB with green→violet gradient and glow halo. */
@Composable
fun AuroraFloatingAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: String = "+",
) {
    val colors = AuroraTheme.colors
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.AuroraGreen, colors.AuroraViolet)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, style = AuroraTheme.type.displaySection, color = colors.VoidBase)
    }
}