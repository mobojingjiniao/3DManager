package com.threed.manager.feature.avatars.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraBottomBar
import com.threed.manager.core.design.components.AuroraButton
import com.threed.manager.core.design.components.AuroraButtonSize
import com.threed.manager.core.design.components.AuroraButtonVariant
import com.threed.manager.core.design.components.AuroraCard
import com.threed.manager.core.design.components.AuroraChip
import com.threed.manager.core.design.components.AuroraFloatingAction
import com.threed.manager.core.design.components.AuroraTopBar
import com.threed.manager.core.design.components.ChipTone
import com.threed.manager.core.design.theme.AuroraTheme
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.model.PosePresets
import com.threed.manager.feature.avatars.model.PoseKeyframe
import com.threed.manager.feature.avatars.ui.library.humanBytes
import com.threed.manager.feature.avatars.ui.library.humanSplatCount

@Composable
fun AvatarDetailScreen(
    avatarId: AvatarId,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onOpenMap: () -> Unit,
    vm: AvatarDetailViewModel = viewModel(),
) {
    val avatar by vm.get(avatarId).collectAsStateWithLifecycle(initialValue = null)
    val colors = AuroraTheme.colors
    val av = avatar
    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = av?.name ?: "Avatar", onBack = onBack)
            if (av == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Avatar not found", color = colors.TextSecondary, style = AuroraTheme.type.bodyMd)
                }
            } else {
                DetailContent(
                    avatar = av,
                    onShare = onShare,
                    onEdit = onEdit,
                    onOpenMap = onOpenMap,
                )
            }
            AuroraBottomBar(
                currentRoute = "avatars",
                onNavigate = {},
                fab = { AuroraFloatingAction(onClick = {}) },
            )
        }
    }
}

@Composable
private fun DetailContent(
    avatar: Avatar,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val colors = AuroraTheme.colors
    var selectedPoseId by remember { mutableStateOf(avatar.metadata.posePreset ?: "idle") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        // Viewer placeholder (procedural particle silhouette)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AuroraTheme.radius.l))
                .background(colors.VoidRaised)
                .border(1.dp, colors.SurfaceBorder, RoundedCornerShape(AuroraTheme.radius.l)),
        ) {
            ViewerPlaceholder(seed = avatar.id.raw.hashCode())
            // Telemetry HUD chip top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.VoidDeep.copy(alpha = 0.85f))
                    .border(1.dp, colors.SurfaceBorderHi, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("FPS  60", color = colors.AuroraGreen, style = AuroraTheme.type.monoSm)
                    Text("SPLATS  ${humanSplatCount(avatar.metadata.splatCount)}", color = colors.TextPrimary, style = AuroraTheme.type.monoSm)
                    Text("TIER  HIGH", color = colors.AuroraGreen, style = AuroraTheme.type.monoSm)
                }
            }
            // Orbit indicator top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.VoidDeep.copy(alpha = 0.85f))
                    .border(1.dp, colors.SurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YAW", color = colors.TextSecondary, style = AuroraTheme.type.overline)
                    Text("+0°", color = colors.AuroraGreen, style = AuroraTheme.type.monoMd.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Name + chips
        Text(avatar.name, color = colors.TextPrimary, style = AuroraTheme.type.displayTitle)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AuroraChip(
                text = avatar.source.name,
                tone = if (avatar.source.name == "CAPTURED") ChipTone.Green else ChipTone.Violet,
                leadingDot = true,
            )
            AuroraChip(text = avatar.metadata.format.name, tone = ChipTone.Violet)
            if (avatar.metadata.locationName != null) {
                AuroraChip(text = avatar.metadata.locationName, tone = ChipTone.Green, leadingDot = true)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = avatar.metadata.locationLat?.let { lat ->
                avatar.metadata.locationLng?.let { lng ->
                    "%.4f° %s  ·  %.4f° %s  ·  ±%.0fm".format(
                        kotlin.math.abs(lat), if (lat >= 0) "N" else "S",
                        kotlin.math.abs(lng), if (lng >= 0) "E" else "W",
                        avatar.metadata.locationAccuracyM ?: 0f,
                    )
                }
            } ?: "no location",
            color = colors.TextMono,
            style = AuroraTheme.type.monoSm,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${avatar.metadata.captureDevice ?: "Unknown"}  ·  ${humanBytes(avatar.metadata.fileBytes)}",
            color = colors.TextMono,
            style = AuroraTheme.type.monoSm,
        )

        Spacer(Modifier.height(16.dp))

        // Mini map preview (if avatar has location)
        if (avatar.metadata.hasLocation) {
            Text("CAPTURED HERE", color = colors.TextMono, style = AuroraTheme.type.overline)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.MapBase),
            ) {
                MapPreview(seed = avatar.id.raw.hashCode())
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.VoidDeep.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "%.4f° N\n%.4f° E".format(avatar.metadata.locationLat!!, avatar.metadata.locationLng!!),
                        color = colors.TextSecondary,
                        style = AuroraTheme.type.monoSm,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clickable(onClick = onOpenMap)
                        .padding(horizontal = 4.dp),
                ) {
                    Text("OPEN IN MAP →", color = colors.AuroraGreen, style = AuroraTheme.type.overline)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Pose picker
        Text("POSE", color = colors.TextMono, style = AuroraTheme.type.overline)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PosePresets.all) { pose ->
                PoseCell(
                    name = pose.displayName,
                    selected = selectedPoseId == pose.id,
                    onClick = { selectedPoseId = pose.id },
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuroraButton(
                text = "Edit",
                onClick = onEdit,
                variant = AuroraButtonVariant.Outline,
                size = AuroraButtonSize.MD,
                modifier = Modifier.weight(1f),
            )
            AuroraButton(
                text = "Wallpaper",
                onClick = {},
                variant = AuroraButtonVariant.OutlineViolet,
                size = AuroraButtonSize.MD,
                modifier = Modifier.weight(1f),
            )
            AuroraButton(
                text = "Share",
                onClick = onShare,
                variant = AuroraButtonVariant.FilledGradient,
                size = AuroraButtonSize.MD,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PoseCell(name: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AuroraTheme.colors
    Box(
        modifier = Modifier
            .size(width = 96.dp, height = 120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) colors.AuroraViolet.copy(alpha = 0.20f) else colors.SurfaceGlass)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.AuroraViolet else colors.SurfaceBorder,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = if (selected) colors.AuroraViolet else colors.AuroraGreen,
                style = AuroraTheme.type.displaySection.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = name,
                color = if (selected) colors.TextPrimary else colors.TextSecondary,
                style = AuroraTheme.type.caption.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun ViewerPlaceholder(seed: Int) {
    val colors = AuroraTheme.colors
    Box(modifier = Modifier.fillMaxSize()) {
        // Particle silhouette placeholder
        val rnd = java.util.Random(seed.toLong())
        // Head
        for (i in 0 until 60) {
            val cx = 0.5f + (rnd.nextFloat() - 0.5f) * 0.30f
            val cy = 0.20f + (rnd.nextFloat() - 0.5f) * 0.20f
            val r = 1.5f + rnd.nextFloat() * 3f
            val c = if (rnd.nextFloat() < 0.5f) colors.AuroraGreen else colors.AuroraViolet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = (cx * 100).dp, top = (cy * 100).dp)
                    .size(r.dp)
                    .clip(CircleShape)
                    .background(c.copy(alpha = 0.85f)),
            )
        }
        // Body
        for (i in 0 until 140) {
            val cx = 0.5f + (rnd.nextFloat() - 0.5f) * 0.45f
            val cy = 0.45f + (rnd.nextFloat() - 0.5f) * 0.50f
            val r = 1.5f + rnd.nextFloat() * 2.5f
            val c = if (rnd.nextFloat() < 0.5f) colors.AuroraGreen else colors.AuroraViolet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = (cx * 100).dp, top = (cy * 100).dp)
                    .size(r.dp)
                    .clip(CircleShape)
                    .background(c.copy(alpha = 0.85f)),
            )
        }
    }
}

@Composable
private fun MapPreview(seed: Int) {
    val colors = AuroraTheme.colors
    Box(modifier = Modifier.fillMaxSize()) {
        // Pseudo-coordinates
        val rnd = java.util.Random(seed.toLong())
        // Center pin (current avatar)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.AuroraGreen.copy(alpha = 0.20f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.VoidDeep)
                .border(2.dp, colors.AuroraGreen, CircleShape),
        )
        // Other avatars (smaller)
        for (i in 0 until 4) {
            val x = rnd.nextFloat()
            val y = rnd.nextFloat()
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = (x * 240).dp, top = (y * 80).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.AuroraViolet),
            )
        }
        // Dashed connecting path
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(40.dp),
        )
        Text(
            text = "·",
            color = colors.AuroraGreen.copy(alpha = 0.4f),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}