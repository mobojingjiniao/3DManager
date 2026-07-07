package com.threed.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threed.manager.core.design.Theme
import com.threed.manager.core.sensor.CameraState
import com.threed.manager.core.sensor.RoamingController
import kotlinx.coroutines.flow.collectLatest

/**
 * Main entry: 3DGS-style procedural background + live camera telemetry.
 *
 * The background is a Compose Canvas that draws 480 synthetic Gaussian
 * splats in 3D, projected to 2D using the camera state produced by
 * [RoamingController.feedSensor] (driven by gravity via
 * `adb emu sensor set acceleration ...` on the dev emulator).
 *
 * Phase 1.3+ swaps the procedural canvas for the real Spark WebView;
 * the layout / sensor plumbing stays identical.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ThreeDManagerApp
        setContent {
            Theme {
                DemoScreen(app = app)
            }
        }
    }
}

@Composable
private fun DemoScreen(app: ThreeDManagerApp) {
    val splatState by app.splatController.state.collectAsState()
    val camera by app.roamingController.camera.collectAsState()

    // Bridge the gravity flow into RoamingController so device tilt
    // drives the camera. The flow stops when the activity is gone.
    LaunchedEffect(app) {
        app.sensorSource.rotation.collectLatest { q ->
            app.roamingController.feedSensor(q)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Procedural 3DGS background — also serves as the surface for
        // the Compose UI overlay.
        SplatBackground(roamingController = app.roamingController)

        // Telemetry overlay (bottom-left, semi-transparent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
        ) {
            Text(
                text = "3DManager",
                color = Color.White,
                fontSize = 36.sp,
                style = MaterialTheme.typography.h4,
            )
            Spacer(Modifier.height(12.dp))
            TelemetryLine("SplatController", splatState::class.simpleName ?: "?")
            TelemetryLine("Camera", camera.format())
            TelemetryLine("RoamingMode", app.roamingController.modeFlow.value.toString().substringAfterLast("."))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "adb emu sensor set acceleration 6.93:0:6.93 → tilt 45°",
                color = Color(0xFFB5B5B5),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun TelemetryLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        color = Color(0xFFE8EAED),
        fontSize = 14.sp,
    )
}

private fun CameraState.format(): String =
    "yaw=${"%.1f".format(yawDeg)}°  pitch=${"%.1f".format(pitchDeg)}°  eye=(${"%.1f".format(eyeX)},${"%.1f".format(eyeY)},${"%.1f".format(eyeZ)})"