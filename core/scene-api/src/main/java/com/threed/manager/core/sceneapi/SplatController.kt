package com.threed.manager.core.sceneapi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Domain controller for a single 3DGS scene.
 *
 * Single source of truth for "what scene is currently shown".
 * The active render backend (Web or Native) is selected at construction
 * by passing the corresponding [WebBridge] / [SplatRendererApi]. Phases
 * 1.3 / 5 will provide those concrete implementations.
 *
 * Phase 1.2 (GREEN): minimum logic for the three RED tests in
 * SplatControllerTest.  Phase 3 adds edit/undo, Phase 4 adds wallpaper
 * hookup.
 */
class SplatController(
    private val bridge: WebBridge,
    private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow<SplatSceneState>(SplatSceneState.Idle)

    /** Hot stream consumed by feature/scenes + feature/editor ViewModels. */
    val state: StateFlow<SplatSceneState> = _state.asStateFlow()

    /**
     * Begin loading the scene at [path].
     *
     * Implementation:
     *  1. Emit [SplatSceneState.Loading]
     *  2. Call [WebBridge.loadScene]
     *  3. Resolve success → [SplatSceneState.Loaded]
     *     Failure → [SplatSceneState.Error]
     */
    fun loadScene(path: String) {
        scope.launch {
            _state.value = SplatSceneState.Loading(path)
            val manifest = SceneManifestJson(
                id = path,
                path = path,
                format = inferFormat(path),
                estimatedSplats = -1L,
            )
            bridge.loadScene(manifest).fold(
                onSuccess = { _state.value = SplatSceneState.Loaded(path, splatCount = manifest.estimatedSplats) },
                onFailure = { cause -> _state.value = SplatSceneState.Error(cause) },
            )
        }
    }

    /** Minimal format inference from extension. Phase 1.3 replaces with real AssetDecoder hookup. */
    private fun inferFormat(path: String): String = when {
        path.endsWith(".ksplat", ignoreCase = true) -> "ksplat"
        path.endsWith(".splat", ignoreCase = true) -> "splat"
        path.endsWith(".spz", ignoreCase = true) -> "spz"
        path.endsWith(".ply", ignoreCase = true) -> "ply"
        path.endsWith(".sog", ignoreCase = true) -> "sog"
        else -> "unknown"
    }
}
