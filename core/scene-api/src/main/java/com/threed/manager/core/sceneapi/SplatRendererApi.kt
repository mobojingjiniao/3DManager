package com.threed.manager.core.sceneapi

/**
 * GPU-facing surface used by the native render backend (Filament) and
 * mirrored by the Web backend (Spark / three.js via [WebBridge]).
 *
 * Pulled into its own interface so unit tests can drive [SplatController]
 * with fakes without spinning up a WebView or a native renderer.
 *
 * Phase 1.x will add: loadSplat / getNodeCount / setNodeOpacity / setNodeColor /
 * transformNode / prune / setEnvironment — each returns [Result] for
 * error propagation identical to [WebBridge].
 */
interface SplatRendererApi {
    // Phase 1.x: real signatures
}
