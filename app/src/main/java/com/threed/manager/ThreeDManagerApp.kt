package com.threed.manager

import android.app.Application
import com.threed.manager.core.sensor.FakeSensorSource
import com.threed.manager.core.sensor.GravityVector
import com.threed.manager.core.sensor.RoamingController
import com.threed.manager.core.sensor.RoamingMode
import com.threed.manager.core.sensor.RotationVector
import com.threed.manager.core.sceneapi.SplatController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Root Application for 3DManager.
 *
 * Holds a single shared instance of the demo wiring:
 *  - [FakeSensorSource] for synthetic IMU events
 *  - [SplatController] backed by a no-op WebBridge (Phase 1.3 swap-in)
 *  - [RoamingController] for camera state
 *
 * In Phase 1.3+ this class wires the real Capacitor-backed WebBridge
 * and AndroidSensorSource via Hilt; for now it serves as a hand-wired
 * service locator so the emulator can verify the architecture end-to-end.
 */
class ThreeDManagerApp : Application() {

    val sensorSource: FakeSensorSource by lazy { FakeSensorSource() }
    val splatController: SplatController by lazy {
        SplatController(bridge = NoOpWebBridge(), scope = CoroutineScope(SupervisorJob()))
    }
    val roamingController: RoamingController by lazy {
        RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 5f)
    }

    override fun onCreate() {
        super.onCreate()
        // Seed the demo with synthetic sensor data so the architecture
        // is observable from the very first frame.
        sensorSource.emitGravity(GravityVector(0f, 0f, -9.81f))
        sensorSource.emitRotation(RotationVector(0f, 0f, 0f, 1f))
    }
}

/** Placeholder WebBridge that pretends a scene loaded successfully. */
private class NoOpWebBridge : com.threed.manager.core.sceneapi.WebBridge {
    override fun loadScene(manifest: com.threed.manager.core.sceneapi.SceneManifestJson) =
        Result.success(Unit)
    override fun setCamera(camera: com.threed.manager.core.sceneapi.CameraJson) =
        Result.success(Unit)
    override fun applyEdit(command: com.threed.manager.core.sceneapi.EditCommandJson) =
        Result.success(Unit)
}