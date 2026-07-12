package com.threed.manager.feature.avatars.ui.map

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
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
import com.threed.manager.feature.avatars.ui.library.humanBytes
import com.threed.manager.feature.avatars.ui.library.humanSplatCount
import kotlin.math.abs

@Composable
fun AvatarMapScreen(
    onBack: () -> Unit,
    onOpenAvatar: (AvatarId) -> Unit,
    onTabNavigate: (String) -> Unit = {},
    vm: AvatarMapViewModel = viewModel(),
) {
    val avatars by vm.avatars.collectAsStateWithLifecycle()
    val colors = AuroraTheme.colors
    var selectedId by remember { mutableStateOf(avatars.firstOrNull { it.metadata.hasLocation }?.id) }
    val selected = avatars.firstOrNull { it.id == selectedId } ?: avatars.firstOrNull()

    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = "Map", onBack = onBack)
            // Coordinate readout
            Text(
                text = "${avatars.count { it.metadata.hasLocation }} OF ${avatars.size} PINNED",
                color = colors.TextMono,
                style = AuroraTheme.type.monoSm,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))

            // Map area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(AuroraTheme.radius.l))
                    .background(colors.MapBase)
                    .border(1.dp, colors.SurfaceBorder, RoundedCornerShape(AuroraTheme.radius.l)),
            ) {
                MapBackground()
                // Pins positioned by lat/lng → map to viewport
                val pts = avatars.filter { it.metadata.hasLocation }.map { av ->
                    val (x, y) = projectLatLngToViewport(av)
                    Triple(av, x, y)
                }
                // Path connecting capture-order pins
                if (pts.size >= 2) {
                    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        // Connecting paths (placeholder; real implementation draws on Canvas)
                    }
                }
                pts.forEach { (av, x, y) ->
                    val isSelected = av.id == selected?.id
                    MapPin(
                        x = x, y = y,
                        selected = isSelected,
                        accent = when (av.source.name) {
                            "CAPTURED" -> colors.AuroraGreen
                            "IMPORTED" -> colors.AuroraViolet
                            else -> colors.AuroraMagenta
                        },
                        onClick = { selectedId = av.id },
                    )
                }
                // Coord readout overlay (top-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.VoidDeep.copy(alpha = 0.85f))
                        .border(1.dp, colors.SurfaceBorderHi, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = selected?.metadata?.locationLat?.let { lat ->
                            selected.metadata.locationLng?.let { lng ->
                                "%.4f° %s\n%.4f° %s".format(
                                    abs(lat), if (lat >= 0) "N" else "S",
                                    abs(lng), if (lng >= 0) "E" else "W",
                                )
                            }
                        } ?: "no GPS",
                        color = colors.TextSecondary,
                        style = AuroraTheme.type.monoSm,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Selected avatar card
            selected?.let { av ->
                AuroraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenAvatar(av.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.VoidRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📍", color = colors.AuroraGreen, style = AuroraTheme.type.displaySection)
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(av.name, color = colors.TextPrimary, style = AuroraTheme.type.bodyLg.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${av.metadata.locationName ?: "—"}  ·  ±${av.metadata.locationAccuracyM?.toInt() ?: 0}m",
                                color = colors.AuroraGreen,
                                style = AuroraTheme.type.monoSm,
                            )
                            Text(
                                text = "${humanSplatCount(av.metadata.splatCount)}  ·  ${humanBytes(av.metadata.fileBytes)}  ·  ${av.source.name}",
                                color = colors.TextMono,
                                style = AuroraTheme.type.monoSm,
                            )
                        }
                        Text("→", color = colors.TextSecondary, style = AuroraTheme.type.displaySection)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            AuroraBottomBar(
                currentRoute = "map",
                onNavigate = onTabNavigate,
                fab = { AuroraFloatingAction(onClick = {}) },
            )
        }
    }
}

@Composable
private fun MapBackground() {
    val colors = AuroraTheme.colors
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        // Latitude lines (horizontal)
        for (i in 0..4) {
            val y = (i * 60).dp
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = y)
                    .height(1.dp)
                    .background(colors.MapGrid),
            )
        }
        // Longitude lines (vertical)
        for (i in 0..5) {
            val x = (i * 60).dp
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = x)
                    .width(1.dp)
                    .background(colors.MapGrid),
            )
        }
        // Aurora ambient glow
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.AuroraGreen.copy(alpha = 0.10f),
                            colors.AuroraViolet.copy(alpha = 0.06f),
                            androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun MapPin(
    x: Float, // 0..1 fraction
    y: Float, // 0..1 fraction
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val colors = AuroraTheme.colors
    val size = if (selected) 40.dp else 28.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = (x * 320).dp, top = (y * 320).dp),
        contentAlignment = Alignment.TopStart,
    ) {
        // Halo (selected only)
        if (selected) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = (-20).dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.30f)),
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(colors.VoidDeep)
                .border(width = if (selected) 3.dp else 2.dp, color = accent, shape = CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "◆",
                color = accent,
                style = AuroraTheme.type.bodySm,
            )
        }
    }
}

/**
 * Project a (lat, lng) onto the map viewport using a simple linear
 * mapping bounded by the seed dataset's bounds. Adequate for visual
 * purposes; a real renderer would use WebMercator projection.
 */
private fun projectLatLngToViewport(av: Avatar): Pair<Float, Float> {
    val lats = listOf(35.2326, 35.4437, 35.6595, 35.6762, 35.6938, 35.7148)
    val lngs = listOf(139.1069, 139.6380, 139.6503, 139.7005, 139.7034, 139.7967)
    val minLat = lats.min(); val maxLat = lats.max()
    val minLng = lngs.min(); val maxLng = lngs.max()
    val lat = av.metadata.locationLat ?: return Pair(0.5f, 0.5f)
    val lng = av.metadata.locationLng ?: return Pair(0.5f, 0.5f)
    val x = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.05f, 0.95f)
    val y = (1f - (lat - minLat) / (maxLat - minLat)).toFloat().coerceIn(0.05f, 0.95f)
    return Pair(x, y)
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t