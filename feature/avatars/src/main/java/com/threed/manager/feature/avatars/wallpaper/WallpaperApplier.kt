package com.threed.manager.feature.avatars.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.SplatFormat
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Apply an avatar as the system wallpaper via [WallpaperManager].
 *
 * Two paths:
 *   - **Live wallpaper** (preferred): launches the live wallpaper
 *     picker targeting [WallpaperTargetService], so the existing
 *     [com.threed.manager.core.system.wallpaper.ThreeDManagerWallpaperService]
 *     takes over and animates the splat cloud with sensor input.
 *   - **Static bitmap** (fallback): renders an aurora-gradient
 *     portrait of the avatar (deterministic from the avatar id) and
 *     hands it to WallpaperManager.setBitmap(...).
 *
 * Phase 0: live-wallpaper route is the only path users actually see.
 * The static-bitmap fallback exists for devices where the AOSP live
 * wallpaper picker rejects our component (rare on real hardware).
 */
class WallpaperApplier(private val context: Context) {

    private val wallpaperManager: WallpaperManager = context.getSystemService()!!

    /** True if the live wallpaper is currently selected. */
    val isLiveWallpaperActive: Boolean
        get() = try {
            wallpaperManager.wallpaperInfo?.component?.className ==
                "com.threed.manager.core.system.wallpaper.ThreeDManagerWallpaperService"
        } catch (e: Exception) { false }

    /**
     * Launch the system's live-wallpaper picker with our service pre-selected.
     * Result delivered to [onResult] (true = user accepted, false = cancelled).
     */
    fun launchLiveWallpaperPicker(): Intent {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(
                    context.packageName,
                    "com.threed.manager.core.system.wallpaper.ThreeDManagerWallpaperService",
                ),
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

    /**
     * Render an aurora-gradient bitmap portrait for [avatar] and set it
     * as the system wallpaper via [WallpaperManager.setBitmap].
     *
     * Returns the path of the bitmap that was installed.
     */
    fun applyStaticBitmap(avatar: Avatar): Result<String> = runCatching {
        val bitmap = renderAvatarBitmap(avatar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        } else {
            @Suppress("DEPRECATION")
            wallpaperManager.setBitmap(bitmap)
        }
        val outFile = File(context.cacheDir, "wallpaper_${avatar.id.raw}.png")
        FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
        outFile.absolutePath
    }.onFailure { Log.w(TAG, "setBitmap failed: ${it.message}") }

    /**
     * Render an aurora-gradient bitmap portrait of the avatar. The
     * particle layout is deterministic from [Avatar.id] so the same
     * avatar always looks the same as wallpaper.
     */
    private fun renderAvatarBitmap(avatar: Avatar): Bitmap {
        val w = 1080
        val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        // Background gradient: aurora green → violet → void
        canvas.drawPaint(Paint().apply {
            shader = RadialGradient(
                w / 2f, h * 0.4f, w.toFloat(),
                intArrayOf(Color.parseColor("#6EFFB7"), Color.parseColor("#9B5CFF"), Color.parseColor("#06070F")),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP,
            )
        })
        // Particle silhouette (deterministic by id)
        val rnd = Random(avatar.id.raw.hashCode().toLong())
        val accent = when (avatar.source.name) {
            "CAPTURED" -> Color.parseColor("#6EFFB7")
            "IMPORTED" -> Color.parseColor("#9B5CFF")
            else -> Color.parseColor("#FF5BD6")
        }
        val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.FILL
        }
        val cx = w / 2f
        val cy = h * 0.55f
        val n = 600
        for (i in 0 until n) {
            // Distribute points within an ellipse approximating a
            // humanoid outline.
            val theta = (i / n.toFloat()) * 2 * Math.PI
            val radius = 200f + rnd.nextFloat() * 60f
            val ox = cos(theta) * radius
            val oy = sin(theta) * radius * 1.6f
            val px = (cx + ox + rnd.nextFloat() * 30f - 15f).toFloat()
            val py = (cy + oy + rnd.nextFloat() * 30f - 15f).toFloat()
            val r = 2f + rnd.nextFloat() * 4f
            particlePaint.color = if (rnd.nextFloat() < 0.7f) accent else Color.WHITE
            particlePaint.alpha = (180 + rnd.nextInt(75)).coerceAtMost(255)
            canvas.drawCircle(px, py, r, particlePaint)
        }
        // Mono meta strip at bottom
        val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9FA8C7")
            textSize = 28f
        }
        canvas.drawText(avatar.name, 80f, h - 160f, monoPaint)
        val metaPaint = Paint(monoPaint).apply { textSize = 22f }
        canvas.drawText("${avatar.metadata.splatCount} splats · ${avatar.metadata.format.name}", 80f, h - 120f, metaPaint)
        avatar.metadata.locationName?.let { canvas.drawText("📍 $it", 80f, h - 80f, metaPaint) }
        return bmp
    }

    private companion object {
        const val TAG = "WallpaperApplier"
    }
}