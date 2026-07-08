package com.threed.manager

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Phase 4 verification entry point.
 *
 * Uses a native [SplatBackgroundView] (no Compose) so the first frame
 * renders on software-rendered emulators without ANR. The view draws a
 * procedural 3DGS-style point cloud and updates yaw/pitch from the real
 * Android SensorManager (TYPE_GAME_ROTATION_VECTOR). Sensor injection
 * via `adb emu sensor set acceleration ...` drives the rotation
 * end-to-end.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
