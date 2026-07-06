package com.threed.manager.core.sceneapi

import kotlinx.serialization.Serializable

/**
 * Synchronous, JSON-message bridge to the active renderer.
 *
 * Web path: implemented by feature-render-web's Capacitor plugins that
 *   pipe messages into the Spark / three.js canvas.
 * Native path: implemented by feature-render-native that targets Filament.
 * Tests: fake implementations live next to unit tests.
 *
 * Every call returns [Result] so error paths surface to the controller
 * without throwing across the bridge.
 */
interface WebBridge {
    fun loadScene(manifest: SceneManifestJson): Result<Unit>
    fun setCamera(camera: CameraJson): Result<Unit>
    fun applyEdit(command: EditCommandJson): Result<Unit>
}

/**
 * Renderer-agnostic manifest payload. Converted into the appropriate
 * concrete message by the active backend at the bridge boundary.
 */
@Serializable
data class SceneManifestJson(
    val id: String,
    val path: String,
    val format: String,
    val estimatedSplats: Long = -1L,
)

@Serializable
data class CameraJson(val eyeX: Float, val eyeY: Float, val eyeZ: Float,
                      val targetX: Float, val targetY: Float, val targetZ: Float,
                      val upX: Float, val upY: Float, val upZ: Float)

@Serializable
sealed interface EditCommandJson {
    @Serializable
    data class SetOpacity(val nodeIds: List<Int>, val alpha: Float) : EditCommandJson

    @Serializable
    data class SetColor(val nodeIds: List<Int>, val r: Float, val g: Float, val b: Float) : EditCommandJson

    @Serializable
    data class Transform(val nodeIds: List<Int>, val matrix4x4: List<Float>) : EditCommandJson
}
