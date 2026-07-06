package com.threed.manager.core.sceneapi

/**
 * Manually-written test double for [WebBridge].
 *
 * Lives next to the test class because it's test-only; production code
 * uses the real Capacitor / Spark-backed implementation in feature-render-web.
 *
 * Behaviour controlled by hookable callbacks. Defaults: bridge rejects (so a
 * test that forgets to configure it sees a clear Error state, not a silent
 * pass).
 */
class FakeWebBridge : WebBridge {
    var onLoadScene: (SceneManifestJson) -> Result<Unit> = { Result.failure(UnsupportedOperationException()) }
    var onSetCamera: (CameraJson) -> Result<Unit> = { Result.success(Unit) }
    var onApplyEdit: (EditCommandJson) -> Result<Unit> = { Result.success(Unit) }

    override fun loadScene(manifest: SceneManifestJson): Result<Unit> = onLoadScene(manifest)
    override fun setCamera(camera: CameraJson): Result<Unit> = onSetCamera(camera)
    override fun applyEdit(command: EditCommandJson): Result<Unit> = onApplyEdit(command)
}
