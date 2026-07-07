package com.threed.manager.core.model

import kotlinx.serialization.Serializable

/**
 * Local representation of a 3DGS asset. Persisted to Room's `scene`
 * table (Phase 1.x) and used as the in-memory model throughout the
 * scene-list / editor / wallpaper flows.
 */
@Serializable
data class SceneAsset(
    val id: String,
    val name: String,
    val splatCount: Long,
    val sourcePath: String,
    val format: String = "ksplat",
    val thumbnailPath: String? = null,
    val source: SceneSource = SceneSource.Local,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
enum class SceneSource { Local, FormScan, Porin }