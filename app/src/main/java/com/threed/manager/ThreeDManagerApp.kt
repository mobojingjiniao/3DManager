package com.threed.manager

import android.app.Application
import com.threed.manager.core.sensor.AndroidSensorSource
import com.threed.manager.core.sensor.FakeSensorSource
import com.threed.manager.core.sensor.RoamingController
import com.threed.manager.core.sensor.RoamingMode
import com.threed.manager.core.sceneapi.SplatController
import com.threed.manager.feature.avatars.AvatarRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Root Application for 3DManager.
 *
 * Service-locator-style wiring. Holds:
 *  - [sensorSource] : AndroidSensorSource (real SensorManager). Tests +
 *    Compose previews can swap in [FakeSensorSource].
 *  - [splatController] : state machine for the active scene (backed
 *    by a no-op [com.threed.manager.core.sceneapi.WebBridge] until
 *    Phase 1.3 swaps it for the Capacitor plugin).
 *  - [roamingController] : state machine for camera orbit.
 *
 * Phase 1.3+ replaces this with Hilt-injected modules; the surface
 * stays identical.
 */
class ThreeDManagerApp : Application() {

    /**
     * Use the real Android sensor source so `adb emu sensor set
     * acceleration ...` actually drives the camera. Swapping in
     * [FakeSensorSource] in tests / previews keeps the rest of the
     * code path identical.
     */
    val sensorSource: AndroidSensorSource by lazy {
        AndroidSensorSource(
            context = this,
            externalScope = CoroutineScope(SupervisorJob()),
        )
    }

    val splatController: SplatController by lazy {
        SplatController(bridge = NoOpWebBridge(), scope = CoroutineScope(SupervisorJob()))
    }

    val roamingController: RoamingController by lazy {
        RoamingController(mode = RoamingMode.Orbit, sensitivity = 1.0f, deadbandDeg = 5f)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the persistent avatar repository (Room-backed, seeded).
        AvatarRepositoryProvider.init(this)
        // AndroidSensorSource needs an explicit start() to begin
        // receiving sensor events. Without this the flow stays empty.
        sensorSource.start()
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