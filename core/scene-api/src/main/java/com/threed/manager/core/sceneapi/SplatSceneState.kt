package com.threed.manager.core.sceneapi

/**
 * FSM describing the controller's view of the active splat scene.
 *
 * The same state surface drives both the Web and Native render paths.
 * Rendering-side loaders can be re-entered by another [SplatController.loadScene]
 * call; the receiver is responsible for tearing the prior scene down.
 */
sealed interface SplatSceneState {
    /** No scene loaded yet (or controller reset). */
    data object Idle : SplatSceneState

    /** Bridge call in flight. UI shows a skeleton / progress. */
    data class Loading(val path: String) : SplatSceneState

    /** Bridge resolved and the scene is being rendered. */
    data class Loaded(val path: String, val splatCount: Long) : SplatSceneState

    /** Bridge or downstream rejected. UI shows an error affordance + retry. */
    data class Error(val cause: Throwable) : SplatSceneState
}
