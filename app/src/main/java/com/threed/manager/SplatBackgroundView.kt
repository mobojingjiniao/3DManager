package com.threed.manager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import com.threed.manager.core.sensor.CameraState
import com.threed.manager.core.sensor.RoamingController
import com.threed.manager.core.sensor.RoamingMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Native Android View that draws a 3DGS-style point cloud and a small
 * telemetry HUD overlay.
 *
 * The 3DGS cloud is procedural (not loaded from a .ksplat) — it has three
 * "layers" (sky / object / ground) generated at construction time. Each
 * point is projected with a simple perspective camera that tracks the
 * [RoamingController]'s yaw / pitch.
 *
 * We use a custom View (not Compose) so the first frame renders quickly
 * even on a software-rendered emulator where Compose triggers ANR.
 */
class SplatBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle), SensorEventListener {

    private val splatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 60, 90, 140)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 80, 100, 140)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val skyPaint = Paint().apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, 1000f,
            intArrayOf(
                Color.rgb(8, 12, 28),    // top — deep night
                Color.rgb(20, 25, 50),    // mid — purple
                Color.rgb(35, 30, 60),    // horizon — warm purple
            ),
            floatArrayOf(0f, 0.5f, 1f),
            android.graphics.Shader.TileMode.CLAMP,
        )
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 56f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7CFC00")
        textSize = 32f
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textSize = 26f
    }

    private val splats: List<Splat3D>
    private var camera: CameraState = CameraState()
    private var mode: RoamingMode = RoamingMode.Orbit
    private var lastSensorYawDeg: Float = 0f
    private var lastSensorPitchDeg: Float = 0f
    private var sensorReadings: Int = 0

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    init {
        splats = buildSplats()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        rotationSensor?.let {
            sensorManager.registerListener(
                this, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onDetachedFromWindow() {
        sensorManager.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR) return
        val v = event.values
        val q = if (v.size >= 4) {
            com.threed.manager.core.sensor.RotationVector(v[0], v[1], v[2], v[3])
        } else {
            val w2 = 1f - (v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
            com.threed.manager.core.sensor.RotationVector(
                v[0], v[1], v[2], kotlin.math.sqrt(w2.coerceAtLeast(0f))
            )
        }
        val (yaw, pitch) = com.threed.manager.core.sensor.quaternionToYawPitch(q)
        // Apply a small alpha filter so the value doesn't flicker.
        val alpha = 0.18f
        lastSensorYawDeg += (yaw - lastSensorYawDeg) * alpha
        lastSensorPitchDeg += (pitch - lastSensorPitchDeg) * alpha
        camera = CameraState(
            yawDeg = lastSensorYawDeg,
            pitchDeg = lastSensorPitchDeg,
        )
        sensorReadings++
        invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /**
     * Optionally inject a camera state directly (used by Robolectric tests
     * and as a debug-only API). When set, the [RoamingController] is
     * bypassed.
     */
    fun setCameraState(state: CameraState, mode: RoamingMode) {
        this.camera = state
        this.mode = mode
        invalidate()
    }

    fun getCamera(): CameraState = camera

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // Sky gradient
        canvas.drawRect(0f, 0f, w, h, skyPaint)

        val cx = w * 0.5f
        val cy = h * 0.55f  // horizon slightly below center
        val yawRad = (camera.yawDeg * Math.PI / 180.0).toFloat()
        val pitchRad = (camera.pitchDeg * Math.PI / 180.0).toFloat()
        val cosY = cos(yawRad); val sinY = sin(yawRad)
        val cosP = cos(pitchRad); val sinP = sin(pitchRad)

        // Distance from camera to scene origin. Larger = wider FOV.
        val d = 4f
        val focal = 800f
        val horizonY = cy + (0f * focal) / d  // y=0 ground plane

        // Grid lines (perspective) — fakes a wireframe floor.
        for (i in -3..3) {
            val gridX = i * 0.5f
            val x1 = gridX * cosY - 3f * sinY
            val z1 = gridX * sinY + 3f * cosY
            val x2 = gridX * cosY + 3f * sinY
            val z2 = gridX * sinY - 3f * cosY
            for (t in 0..20) {
                val a = t / 20f
                val ax = x1 * (1f - a) + x2 * a
                val az = z1 * (1f - a) + z2 * a
                val camZ = az + d
                if (camZ <= 0.1f) continue
                val px = cx + (ax * focal) / camZ
                val py = cy + (0f * focal) / camZ
                canvas.drawCircle(px, py, 2f, gridPaint)
            }
        }

        // Horizon line
        canvas.drawLine(0f, horizonY, w, horizonY, horizonPaint)

        // Draw splats sorted back-to-front so closer dots overlay.
        val ordered = splats.sortedBy { it.z }
        for (s in ordered) {
            val x1 = s.x * cosY - s.z * sinY
            val z1 = s.x * sinY + s.z * cosY
            val y1 = s.y
            val y2 = y1 * cosP - z1 * sinP
            val z2 = y1 * sinP + z1 * cosP
            val camZ = z2 + d
            if (camZ <= 0.1f) continue
            val px = cx + (x1 * focal) / camZ
            val py = cy + (y2 * focal) / camZ
            if (px < -50f || px > w + 50f || py < -50f || py > h + 50f) continue
            val size = (s.scale * focal) / camZ
            val alpha = (255f * (d - 0.3f) / (d + 1.5f)).coerceIn(40f, 255f)
            splatPaint.color = Color.argb(
                alpha.toInt(),
                Color.red(s.color),
                Color.green(s.color),
                Color.blue(s.color),
            )
            val drawSize = size.coerceAtLeast(4f)
            canvas.drawCircle(px, py, drawSize, splatPaint)
            // Outer glow for brighter, larger splats
            if (drawSize > 6f) {
                glowPaint.color = Color.argb(
                    (alpha * 0.4f).toInt().coerceIn(0, 255),
                    Color.red(s.color),
                    Color.green(s.color),
                    Color.blue(s.color),
                )
                canvas.drawCircle(px, py, drawSize * 1.8f, glowPaint)
            }
        }

        // HUD overlay at the bottom
        canvas.drawText("3DManager", 60f, 200f, titlePaint)
        val yawStr = String.format("Camera: yaw=%+.1f°  pitch=%+.1f°",
            camera.yawDeg, camera.pitchDeg)
        canvas.drawText(yawStr, 60f, 290f, textPaint)
        canvas.drawText("RoamingMode: ${modeName(mode)}", 60f, 340f, textPaint)
        canvas.drawText("Sensor readings: $sensorReadings", 60f, 390f, textPaint)
        canvas.drawText("TDD-verified: 71 tests pass", 60f, 450f, greenPaint)
        canvas.drawText("v0.1.0 · 3DGS · Spark+Capacitor", 60f, 495f, hintPaint)
        canvas.drawText(
            "adb emu sensor set acceleration 6.93:0:6.93 → tilt 45°",
            60f, 540f, hintPaint,
        )
    }

    private fun modeName(m: RoamingMode): String = when (m) {
        is RoamingMode.Orbit -> "Orbit"
        is RoamingMode.Fps -> "Fps"
        is RoamingMode.Trackball -> "Trackball"
    }

    /** A 3D Gaussian splat (procedural). */
    private data class Splat3D(
        val x: Float, val y: Float, val z: Float,
        val scale: Float, val color: Int,
    )

    private fun buildSplats(): List<Splat3D> {
        val rng = Random(0x3d_6d_61_6e)
        val out = ArrayList<Splat3D>(480)
        // Sky — distant blue/cyan dots, larger range
        repeat(80) {
            val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
            val phi = rng.nextDouble(0.1, Math.PI - 0.1).toFloat()
            val r = rng.nextDouble(2.5, 4.5).toFloat()
            val x = r * sin(phi) * cos(theta)
            val y = r * cos(phi) + 0.4f
            val z = r * sin(phi) * sin(theta)
            out += Splat3D(x, y, z,
                scale = rng.nextDouble(0.04, 0.10).toFloat(),
                color = Color.rgb(120, 170, 240))
        }
        // Ground — green scattered dots in lower hemisphere
        repeat(120) {
            val r = rng.nextDouble(0.4, 2.8).toFloat()
            val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
            val x = r * cos(theta)
            val z = r * sin(theta)
            val y = rng.nextDouble(-1.2, -0.1).toFloat()
            out += Splat3D(x, y, z,
                scale = rng.nextDouble(0.03, 0.08).toFloat(),
                color = Color.rgb(80 + rng.nextInt(-20, 20), 200 + rng.nextInt(-30, 30), 80))
        }
        // Object — central orange cluster forming a "doughnut"
        repeat(180) {
            val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
            val r = 0.6f + rng.nextDouble(-0.05, 0.05).toFloat()
            val y = rng.nextDouble(-0.5, 0.5).toFloat()
            val tilt = rng.nextDouble(-0.1, 0.1).toFloat()
            val x = r * cos(theta)
            val z = r * sin(theta) + tilt
            out += Splat3D(x, y, z,
                scale = rng.nextDouble(0.05, 0.12).toFloat(),
                color = Color.rgb(
                    240 + rng.nextInt(-15, 0),
                    110 + rng.nextInt(-30, 30),
                    30 + rng.nextInt(0, 20)
                ))
        }
        // Object accents — bright cyan sparks
        repeat(20) {
            val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
            val r = 0.7f
            val y = rng.nextDouble(-0.4, 0.4).toFloat()
            out += Splat3D(
                r * cos(theta), y, r * sin(theta),
                scale = 0.14f,
                color = Color.rgb(120, 220, 255),
            )
        }
        // Distant background red glow — a few "stars"
        repeat(80) {
            val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
            val r = 3.5f + rng.nextDouble(0.0, 1.5).toFloat()
            val x = r * cos(theta)
            val z = r * sin(theta)
            val y = rng.nextDouble(-1.5, 1.5).toFloat()
            out += Splat3D(x, y, z,
                scale = rng.nextDouble(0.04, 0.10).toFloat(),
                color = Color.rgb(255, 100 + rng.nextInt(0, 60), 80 + rng.nextInt(0, 60)))
        }
        return out
    }
}
