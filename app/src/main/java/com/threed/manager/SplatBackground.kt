package com.threed.manager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.threed.manager.core.sensor.CameraState
import com.threed.manager.core.sensor.RoamingController
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3DGS-style background.
 *
 * Generates [count] synthetic Gaussian splats (positions + colors +
 * scales) and projects them to 2D based on the active
 * [RoamingController.camera]. This is **not** a real 3DGS renderer
 * (that requires Spark / Filament), but it gives the emulator a
 * visual analog that rotates with gravity so the architecture is
 * observable end-to-end.
 *
 * Phase 1.3+ replaces this with Spark's [SplatMesh] rendering the
 * real asset; the projection math here is the same.
 */
@Composable
fun SplatBackground(
    roamingController: RoamingController,
    count: Int = 480,
    seed: Long = 42L,
) {
    val splats = remember(count, seed) { SyntheticSplats.generate(count, seed) }
    val camera by roamingController.camera.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawSplatField(splats, camera)
    }
}

private fun DrawScope.drawSplatField(
    splats: List<SyntheticSplat>,
    camera: CameraState,
) {
    val width = size.width
    val height = size.height
    val cx = width / 2f
    val cy = height / 2f

    // Apply yaw / pitch as rotation around Y and X axis respectively.
    val yawRad = Math.toRadians(camera.yawDeg.toDouble()).toFloat()
    val pitchRad = Math.toRadians(camera.pitchDeg.toDouble()).toFloat()
    val cosY = cos(yawRad); val sinY = sin(yawRad)
    val cosP = cos(pitchRad); val sinP = sin(pitchRad)

    // Project each splat through the rotated frame, then to 2D with
    // a simple pinhole camera.
    data class Projected(val x: Float, val y: Float, val depth: Float, val color: Color, val size: Float)
    val projected = splats.map { splat ->
        // World → camera frame
        val x1 = splat.x * cosY + splat.z * sinY
        val z1 = -splat.x * sinY + splat.z * cosY
        val y1 = splat.y * cosP - z1 * sinP
        val z2 = splat.y * sinP + z1 * cosP
        // Pinhole: x_screen = cx + f * x1 / (z2 + offset), y = cy + f * y1 / (z2 + offset)
        val offset = 8f
        val fov = 600f
        val depth = z2 + offset
        val xScreen = cx + fov * x1 / depth
        val yScreen = cy + fov * y1 / depth
        // Depth-based color attenuation
        val fade = (depth.coerceIn(2f, 20f) - 2f) / 18f
        val color = splat.color.copy(alpha = (1f - fade * 0.6f))
        // Depth-based size (closer = larger)
        val radius = splat.size * (8f / depth).coerceIn(0.3f, 6f)
        Projected(xScreen, yScreen, depth, color, radius)
    }.sortedBy { -it.depth } // back-to-front painter's algorithm

    // Draw splats
    projected.forEach { p ->
        if (p.x in -50f..(width + 50f) && p.y in -50f..(height + 50f)) {
            drawCircle(color = p.color, radius = p.size, center = Offset(p.x, p.y))
            // Soft outer glow
            drawCircle(
                color = p.color.copy(alpha = p.color.alpha * 0.25f),
                radius = p.size * 1.8f,
                center = Offset(p.x, p.y),
            )
        }
    }

    // Horizon line for depth reference
    drawLine(
        color = Color(0xFF1A1D21),
        start = Offset(0f, cy),
        end = Offset(width, cy),
        strokeWidth = 1f,
    )
}

private data class SyntheticSplat(
    val x: Float,
    val y: Float,
    val z: Float,
    val color: Color,
    val size: Float,
)

private object SyntheticSplats {
    fun generate(count: Int, seed: Long): List<SyntheticSplat> {
        val rng = java.util.Random(seed)
        // Lay out splats in a ring (rough 3DGS scene: one object + ground)
        return List(count) {
            val angle = rng.nextDouble() * Math.PI * 2
            val radius = if (rng.nextFloat() < 0.4f) 0.5f + rng.nextFloat() * 1.5f else 1.5f + rng.nextFloat() * 4f
            val height = if (rng.nextFloat() < 0.3f) -2f + rng.nextFloat() * 0.4f else -1.5f + rng.nextFloat() * 3f
            val x = (radius * cos(angle)).toFloat()
            val z = (radius * sin(angle)).toFloat()
            val y = height

            // Color: warm-orange for "object" ring, green/blue for "sky/floor"
            val color = when {
                radius < 2f -> Color(
                    red = 1.0f,
                    green = 0.42f + rng.nextFloat() * 0.2f,
                    blue = 0.21f + rng.nextFloat() * 0.15f,
                    alpha = 0.9f,
                )
                height < -1f -> Color(
                    red = 0.3f + rng.nextFloat() * 0.1f,
                    green = 0.6f + rng.nextFloat() * 0.2f,
                    blue = 0.35f + rng.nextFloat() * 0.15f,
                    alpha = 0.85f,
                )
                else -> Color(
                    red = 0.45f + rng.nextFloat() * 0.2f,
                    green = 0.55f + rng.nextFloat() * 0.2f,
                    blue = 0.85f + rng.nextFloat() * 0.15f,
                    alpha = 0.85f,
                )
            }
            SyntheticSplat(
                x = x,
                y = y,
                z = z,
                color = color,
                size = 0.6f + rng.nextFloat() * 1.0f,
            )
        }
    }
}