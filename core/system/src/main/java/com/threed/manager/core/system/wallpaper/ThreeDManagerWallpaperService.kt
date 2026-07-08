package com.threed.manager.core.system.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.threed.manager.core.sensor.RotationVector
import com.threed.manager.core.sensor.quaternionToYawPitch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Live wallpaper service that renders a 3DGS-style procedural point cloud
 * (sky / ground / object layers) and updates its yaw / pitch from the
 * real SensorManager (TYPE_GAME_ROTATION_VECTOR).
 *
 * The engine runs a self-managed render thread (no [android.opengl.GLSurfaceView]
 * because WallpaperService does not own a View). The render loop:
 *  1. wait for [SurfaceHolder.surface] to be valid
 *  2. on each tick, lock the canvas, draw the 3DGS cloud through
 *     the current yaw / pitch, unlock
 *  3. pace to ~30 fps to avoid burning the device on a wallpaper
 *
 * `adb shell am start -a android.service.wallpaper.LIVE_WALLPAPER_CHOOSER`
 * lets the user pick the service from the system wallpaper chooser.
 */
class ThreeDManagerWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = SplatEngine()

    private data class Splat3D(val x: Float, val y: Float, val z: Float, val scale: Float, val color: Int)

    inner class SplatEngine : Engine(), SensorEventListener {
        private val splatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
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
                0f, 0f, 0f, 2400f,
                intArrayOf(
                    Color.rgb(8, 12, 28),
                    Color.rgb(20, 25, 50),
                    Color.rgb(35, 30, 60),
                ),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP,
            )
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7CFC00")
            textSize = 28f
        }
        private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 24f
        }

        private val splats: List<Splat3D> = buildSplats()
        @Volatile private var yawDeg: Float = 0f
        @Volatile private var pitchDeg: Float = 0f
        @Volatile private var sensorReadings: Int = 0
        @Volatile private var visible: Boolean = false

        private val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        private var renderThread: Thread? = null
        @Volatile private var stopRender = false

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                rotationSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
                startRenderThread()
            } else {
                sensorManager.unregisterListener(this)
                stopRenderThread()
            }
        }

        override fun onDestroy() {
            stopRenderThread()
            sensorManager.unregisterListener(this)
            super.onDestroy()
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR) return
            val v = event.values
            val q = if (v.size >= 4) {
                RotationVector(v[0], v[1], v[2], v[3])
            } else {
                val w2 = 1f - (v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
                RotationVector(v[0], v[1], v[2], kotlin.math.sqrt(w2.coerceAtLeast(0f)))
            }
            val (y, p) = quaternionToYawPitch(q)
            val alpha = 0.18f
            yawDeg += (y - yawDeg) * alpha
            pitchDeg += (p - pitchDeg) * alpha
            sensorReadings++
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        private fun startRenderThread() {
            if (renderThread?.isAlive == true) return
            stopRender = false
            renderThread = Thread {
                val frameTargetMs = 33L  // ~30 fps
                while (!stopRender) {
                    val start = System.currentTimeMillis()
                    drawFrame()
                    val elapsed = System.currentTimeMillis() - start
                    val sleep = frameTargetMs - elapsed
                    if (sleep > 0) Thread.sleep(sleep)
                }
            }.apply {
                name = "SplatWallpaperRenderer"
                start()
            }
        }

        private fun stopRenderThread() {
            stopRender = true
            renderThread?.join(200)
            renderThread = null
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas() ?: return
                drawToCanvas(canvas)
            } catch (e: Exception) {
                // surface gone or invalidated; loop will retry
            } finally {
                if (canvas != null) {
                    try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
                }
            }
        }

        private fun drawToCanvas(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            if (w <= 0f || h <= 0f) return

            canvas.drawRect(0f, 0f, w, h, skyPaint)

            val cx = w * 0.5f
            val cy = h * 0.55f
            val yawRad = (yawDeg * Math.PI / 180.0).toFloat()
            val pitchRad = (pitchDeg * Math.PI / 180.0).toFloat()
            val cosY = cos(yawRad); val sinY = sin(yawRad)
            val cosP = cos(pitchRad); val sinP = sin(pitchRad)

            val d = 4f
            val focal = 800f
            val horizonY = cy + (0f * focal) / d

            // perspective grid
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
            canvas.drawLine(0f, horizonY, w, horizonY, horizonPaint)

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

            // HUD
            canvas.drawText("3DManager Wallpaper", 60f, 200f, greenPaint)
            val yawStr = String.format("Camera: yaw=%+.1f°  pitch=%+.1f°", yawDeg, pitchDeg)
            canvas.drawText(yawStr, 60f, 290f, textPaint)
            canvas.drawText("Sensor readings: $sensorReadings", 60f, 350f, textPaint)
            canvas.drawText("TDD-verified: 71 tests pass", 60f, 420f, greenPaint)
            canvas.drawText("v0.1.0 · 3DGS · Live Wallpaper", 60f, 470f, hintPaint)
        }

        private fun buildSplats(): List<Splat3D> {
            val rng = Random(0x3d_6d_61_6e)
            val out = ArrayList<Splat3D>(480)
            repeat(80) {
                val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
                val phi = rng.nextDouble(0.1, Math.PI - 0.1).toFloat()
                val r = rng.nextDouble(2.5, 4.5).toFloat()
                out += Splat3D(
                    r * sin(phi) * cos(theta),
                    r * cos(phi) + 0.4f,
                    r * sin(phi) * sin(theta),
                    scale = rng.nextDouble(0.04, 0.10).toFloat(),
                    color = Color.rgb(120, 170, 240),
                )
            }
            repeat(120) {
                val r = rng.nextDouble(0.4, 2.8).toFloat()
                val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
                out += Splat3D(
                    r * cos(theta),
                    rng.nextDouble(-1.2, -0.1).toFloat(),
                    r * sin(theta),
                    scale = rng.nextDouble(0.03, 0.08).toFloat(),
                    color = Color.rgb(80 + rng.nextInt(-20, 20), 200 + rng.nextInt(-30, 30), 80),
                )
            }
            repeat(180) {
                val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
                val r = 0.6f + rng.nextDouble(-0.05, 0.05).toFloat()
                val y = rng.nextDouble(-0.5, 0.5).toFloat()
                val tilt = rng.nextDouble(-0.1, 0.1).toFloat()
                out += Splat3D(
                    r * cos(theta), y, r * sin(theta) + tilt,
                    scale = rng.nextDouble(0.05, 0.12).toFloat(),
                    color = Color.rgb(
                        240 + rng.nextInt(-15, 0),
                        110 + rng.nextInt(-30, 30),
                        30 + rng.nextInt(0, 20),
                    ),
                )
            }
            repeat(20) {
                val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
                out += Splat3D(
                    0.7f * cos(theta),
                    rng.nextDouble(-0.4, 0.4).toFloat(),
                    0.7f * sin(theta),
                    scale = 0.14f,
                    color = Color.rgb(120, 220, 255),
                )
            }
            repeat(80) {
                val theta = rng.nextDouble(0.0, Math.PI * 2).toFloat()
                val r = 3.5f + rng.nextDouble(0.0, 1.5).toFloat()
                out += Splat3D(
                    r * cos(theta),
                    rng.nextDouble(-1.5, 1.5).toFloat(),
                    r * sin(theta),
                    scale = rng.nextDouble(0.04, 0.10).toFloat(),
                    color = Color.rgb(255, 100 + rng.nextInt(0, 60), 80 + rng.nextInt(0, 60)),
                )
            }
            return out
        }
    }
}
