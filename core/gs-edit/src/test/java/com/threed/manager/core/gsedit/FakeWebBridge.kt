package com.threed.manager.core.gsedit

import com.threed.manager.core.sceneapi.CameraJson
import com.threed.manager.core.sceneapi.EditCommandJson
import com.threed.manager.core.sceneapi.SceneManifestJson
import com.threed.manager.core.sceneapi.WebBridge

/**
 * Local test double — the production-grade version lives in core/scene-api's
 * test sources; we re-declare here so the gs-edit unit tests don't depend on
 * a test-fixture module.
 */
internal class FakeWebBridge : WebBridge {
    var onLoadScene: (SceneManifestJson) -> Result<Unit> = { Result.success(Unit) }
    var onSetCamera: (CameraJson) -> Result<Unit> = { Result.success(Unit) }
    var onApplyEdit: (EditCommandJson) -> Result<Unit> = { Result.success(Unit) }

    override fun loadScene(manifest: SceneManifestJson): Result<Unit> = onLoadScene(manifest)
    override fun setCamera(camera: CameraJson): Result<Unit> = onSetCamera(camera)
    override fun applyEdit(command: EditCommandJson): Result<Unit> = onApplyEdit(command)
}
