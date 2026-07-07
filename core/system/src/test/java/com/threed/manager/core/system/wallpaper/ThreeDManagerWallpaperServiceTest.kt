package com.threed.manager.core.system.wallpaper

import android.graphics.Canvas
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicReference

/**
 * Phase 4.2 — Robolectric tests for [ThreeDManagerWallpaperService].
 *
 * Uses JUnit 4 + RobolectricTestRunner (the standard robolectric combo
 * that does not require the `robolectric-junit5` artifact, which is not
 * in the offline cache).
 *
 * Drives the [WallpaperService.Engine] lifecycle and asserts the
 * observable state. `surfaceHolder` is not directly accessible from the
 * public Engine API in this SDK level, so the surface callbacks are
 * invoked with a `null` holder (Robolectric records the call regardless
 * of the holder argument).
 */
@RunWith(RobolectricTestRunner::class)
class ThreeDManagerWallpaperServiceTest {

    /** Minimal SurfaceHolder stub for tests. */
    private fun stubHolder(): SurfaceHolder = object : android.view.SurfaceHolder {
        override fun addCallback(callback: android.view.SurfaceHolder.Callback?) {}
        override fun removeCallback(callback: android.view.SurfaceHolder.Callback?) {}
        override fun lockCanvas(): Canvas? = null
        override fun lockCanvas(dirty: android.graphics.Rect?): Canvas? = null
        override fun unlockCanvasAndPost(canvas: Canvas?) {}
        override fun getSurface(): android.view.Surface = throw UnsupportedOperationException()
        override fun getSurfaceFrame(): android.graphics.Rect = android.graphics.Rect()
        override fun setFixedSize(width: Int, height: Int) {}
        override fun setSizeFromLayout() {}
        override fun setFormat(format: Int) {}
        override fun setType(type: Int) {}
        override fun setKeepScreenOn(screenOn: Boolean) {}
        override fun isCreating(): Boolean = false
    }

    private fun createService(): ThreeDManagerWallpaperService.WallpaperEngine {
        val controller = Robolectric.buildService(ThreeDManagerWallpaperService::class.java)
        controller.create()
        return controller.get().onCreateEngine() as ThreeDManagerWallpaperService.WallpaperEngine
    }

    @Test
    fun engineIsNotYetCreatedOnConstruction() {
        val engine = createService()
        assertThat(engine.created).isFalse()
    }

    @Test
    fun engineMarksCreatedWhenSurfaceArrives() {
        val engine = createService()
        engine.onSurfaceCreated(stubHolder())
        assertThat(engine.created).isTrue()
        assertThat(engine.surfaceDestroyed).isFalse()
    }

    @Test
    fun engineMarksSurfaceDestroyedOnTeardown() {
        val engine = createService()
        engine.onSurfaceCreated(stubHolder())
        engine.onSurfaceDestroyed(stubHolder())
        assertThat(engine.surfaceDestroyed).isTrue()
    }

    @Test
    fun visibilityFalseFlipsTierDowngradeThenTrueClears() {
        val engine = createService()
        engine.onVisibilityChanged(false)
        assertThat(engine.tierDowngraded).isTrue()
        assertThat(engine.visible).isFalse()
        engine.onVisibilityChanged(true)
        assertThat(engine.visible).isTrue()
        assertThat(engine.tierDowngraded).isFalse()
    }
}
