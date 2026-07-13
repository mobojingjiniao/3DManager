package com.threed.manager.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence for an Avatar — mirrors the in-memory domain model
 * (see feature/avatars/model/Avatar.kt) with one row per avatar.
 *
 * GPS fields are nullable; avatars without a captured location are
 * still persisted (Pin-on-map is opt-in).
 */
@Entity(tableName = "avatars")
data class AvatarEntity(
    @PrimaryKey val id: String,
    val name: String,
    val source: String,           // CAPTURED / IMPORTED / GENERATED / SAMPLE
    val status: String,           // DRAFT / READY / SYNCING / FAILED
    val splatCount: Long,
    val fileBytes: Long,
    val format: String,           // PLY / SPLAT / KSPLAT / SPZ / SOG / GLB
    val capturedAt: Long?,
    val captureDevice: String?,
    val estimatedHeightCm: Float?,
    val posePreset: String?,
    val tags: String,             // comma-separated
    val locationLat: Double?,
    val locationLng: Double?,
    val locationName: String?,
    val locationAccuracyM: Float?,
    val localFilePath: String?,   // /data/.../avatars/<id>.ksplat
    val favorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)