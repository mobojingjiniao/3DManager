package com.threed.manager.core.system

/** Lightweight scene characteristics that drive tier selection. */
data class SceneMetrics(
    val splatCount: Long,
    val estimatedTextureBytes: Long = 0L,
)
