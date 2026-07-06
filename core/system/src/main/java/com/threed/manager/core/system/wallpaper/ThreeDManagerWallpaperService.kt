package com.threed.manager.core.system.wallpaper

import android.service.wallpaper.WallpaperService

/**
 * Placeholder WallpaperService.
 *
 * Implementation lives in Phase 4 (AdaptiveLiveWallpaperEngine).
 * Here the engine is a no-op so the manifest service reference compiles.
 * AdaptiveRenderStrategy lives in the same package and will be wired up later.
 */
class ThreeDManagerWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = PlaceholderEngine()

    private inner class PlaceholderEngine : Engine() {
        override fun onVisibilityChanged(visible: Boolean) { /* no-op for scaffolding */ }
    }
}
