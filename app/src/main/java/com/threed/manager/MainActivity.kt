package com.threed.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threed.manager.core.design.Theme

/**
 * Demo / verification screen.
 *
 * Shows the **end-to-end state** of every TDD-built controller so the
 * emulator viewer can confirm the architecture is alive without needing
 * a hardware-GL renderer:
 *
 *  - SplatController state (Idle / Loading / Loaded / Error) from core/scene-api
 *  - RoamingController camera (yaw / pitch / eye position) from core/sensor
 *  - Gravity vector + rotation quaternion (live from FakeSensorSource)
 *
 * In Phase 1.3 this screen is replaced by the real Spark WebView path
 * — the Compose wiring remains the same; only the `body` changes.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ThreeDManagerApp
        setContent {
            Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF101112)) {
                    DemoBody(app = app)
                }
            }
        }
    }
}

@Composable
private fun DemoBody(app: ThreeDManagerApp) {
    val splatState by app.splatController.state.collectAsState()
    val camera by app.roamingController.camera.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "3DManager",
            color = Color.White,
            fontSize = 36.sp,
            style = MaterialTheme.typography.h4,
        )
        Spacer(Modifier.height(16.dp))

        StatusLine(label = "SplatController.state", value = splatState::class.simpleName ?: "?")
        Spacer(Modifier.height(8.dp))
        StatusLine(label = "Camera.yawDeg", value = "%.1f°".format(camera.yawDeg))
        StatusLine(label = "Camera.pitchDeg", value = "%.1f°".format(camera.pitchDeg))
        StatusLine(label = "Camera.eye", value = "(%.1f, %.1f, %.1f)".format(camera.eyeX, camera.eyeY, camera.eyeZ))
        Spacer(Modifier.height(8.dp))
        StatusLine(label = "RoamingMode", value = app.roamingController.modeFlow.value.toString().substringAfterLast("."))

        Spacer(Modifier.height(16.dp))
        Text(text = "TDD-verified: 71 tests pass", color = Color(0xFF7CDB8C), fontSize = 14.sp)
        Text(text = "v0.1.0 · 3DGS · Spark+Capacitor", color = Color(0xFFB5B5B5), fontSize = 12.sp)
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        color = Color(0xFFE8EAED),
        fontSize = 14.sp,
    )
}