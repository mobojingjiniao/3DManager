package com.threed.manager.core.system.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * Live wallpaper service for 3DManager.
 *
 * Phase 4.2 wires a real engine lifecycle that:
 *  - defers GL context creation to `onSurfaceCreated` (the OS calls this
 *    when the wallpaper is actually being shown, not on service start)
 *  - pauses rendering on `onVisibilityChanged(false)` to free GPU/CPU
 *  - resumes on `onVisibilityChanged(true)`
 *  - re-creates the GL context on `onSurfaceDestroyed`
 *
 * The actual GL thread + Spark/Filament rendering is owned by an inner
 * `Engine` subclass; the engine is a self-managed EGL surface (not
 * `GLSurfaceView`, which is bound to a `View` and is awkward to use
 * inside a `WallpaperService`).
 *
 * In Phase 4.2 the engine is still a thin state holder — it tracks
 * [created], [visible], [tierDowngraded] without any actual GL
 * work. Phase 5 wires the real renderer in.
 */
class ThreeDManagerWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = WallpaperEngine()

    inner class WallpaperEngine : Engine() {

        @Volatile internal var created: Boolean = false
        @Volatile internal var visible: Boolean = false
        @Volatile internal var surfaceDestroyed: Boolean = false

        /**
         * True when the engine auto-downgraded to a lower [RenderTier]
         * because the user did not interact for 30 s. Tracked here for
         * Phase 5's EGL loop; tested by Robolectric to confirm the
         * service handles the state machine.
         */
        @Volatile internal var tierDowngraded: Boolean = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            // Service-level one-time setup. Phase 5: load Spark JS bundle.
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            created = true
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceDestroyed = true
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (!visible) {
                // 30s idle → drop to NORMAL. We don't actually time here
                // (the OS pauses us anyway); Phase 5 wires the timer.
                tierDowngraded = true
            } else {
                tierDowngraded = false
            }
        }

        override fun onDestroy() {
            created = false
            super.onDestroy()
        }
    }
}
