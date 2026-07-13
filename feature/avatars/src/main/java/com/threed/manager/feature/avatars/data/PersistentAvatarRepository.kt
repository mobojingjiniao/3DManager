package com.threed.manager.feature.avatars.data

import android.content.Context
import com.threed.manager.core.data.db.AvatarDao
import com.threed.manager.core.data.db.AvatarEntity
import com.threed.manager.core.data.db.ThreeDManagerDatabase
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.model.AvatarMetadata
import com.threed.manager.feature.avatars.model.AvatarSource
import com.threed.manager.feature.avatars.model.AvatarStatus
import com.threed.manager.feature.avatars.model.SplatFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Room-backed [AvatarRepository]. Seeds the DB on first launch if empty.
 *
 * Replaces the in-memory [com.threed.manager.feature.avatars.model.AvatarRepository].
 */
class PersistentAvatarRepository(
    context: Context,
) {
    private val dao: AvatarDao = ThreeDManagerDatabase.get(context).avatarDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            if (dao.count() == 0) {
                dao.upsertAll(seed())
            }
        }
    }

    val avatars: StateFlow<List<Avatar>> = dao.observeAll()
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun observe(id: AvatarId): Flow<Avatar?> =
        dao.observeById(id.raw).map { it?.toDomain() }

    suspend fun getById(id: AvatarId): Avatar? =
        dao.getById(id.raw)?.toDomain()

    suspend fun toggleFavorite(id: AvatarId) = dao.toggleFavorite(id.raw)

    suspend fun delete(id: AvatarId) = dao.deleteById(id.raw)

    suspend fun add(avatar: Avatar) = dao.upsert(avatar.toEntity())

    suspend fun updateMetadata(id: AvatarId, metadata: AvatarMetadata) {
        val existing = dao.getById(id.raw) ?: return
        dao.update(existing.copy(
            splatCount = metadata.splatCount,
            fileBytes = metadata.fileBytes,
            format = metadata.format.name,
            capturedAt = metadata.capturedAt,
            captureDevice = metadata.captureDevice,
            estimatedHeightCm = metadata.estimatedHeightCm,
            posePreset = metadata.posePreset,
            tags = metadata.tags.joinToString(","),
            locationLat = metadata.locationLat,
            locationLng = metadata.locationLng,
            locationName = metadata.locationName,
            locationAccuracyM = metadata.locationAccuracyM,
            updatedAt = System.currentTimeMillis(),
        ))
    }

    private fun AvatarEntity.toDomain(): Avatar = Avatar(
        id = AvatarId(id),
        name = name,
        source = AvatarSource.valueOf(source),
        status = AvatarStatus.valueOf(status),
        metadata = AvatarMetadata(
            splatCount = splatCount,
            fileBytes = fileBytes,
            format = SplatFormat.valueOf(format),
            capturedAt = capturedAt,
            captureDevice = captureDevice,
            estimatedHeightCm = estimatedHeightCm,
            posePreset = posePreset,
            tags = tags.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList(),
            locationLat = locationLat,
            locationLng = locationLng,
            locationName = locationName,
            locationAccuracyM = locationAccuracyM,
        ),
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun Avatar.toEntity(): AvatarEntity = AvatarEntity(
        id = id.raw,
        name = name,
        source = source.name,
        status = status.name,
        splatCount = metadata.splatCount,
        fileBytes = metadata.fileBytes,
        format = metadata.format.name,
        capturedAt = metadata.capturedAt,
        captureDevice = metadata.captureDevice,
        estimatedHeightCm = metadata.estimatedHeightCm,
        posePreset = metadata.posePreset,
        tags = metadata.tags.joinToString(","),
        locationLat = metadata.locationLat,
        locationLng = metadata.locationLng,
        locationName = metadata.locationName,
        locationAccuracyM = metadata.locationAccuracyM,
        localFilePath = null,
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun seed(): List<AvatarEntity> {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        return listOf(
            AvatarEntity(
                id = "avt_seed_001", name = "Me", source = "CAPTURED", status = "READY",
                splatCount = 1_247_392, fileBytes = 12_400_000L, format = "KSPLAT",
                capturedAt = now - 2 * day, captureDevice = "Pixel 8 Pro",
                estimatedHeightCm = 1.7f, posePreset = "idle", tags = "",
                locationLat = 35.6762, locationLng = 139.6503,
                locationName = "Tokyo, JP", locationAccuracyM = 4f,
                localFilePath = null, favorite = true,
                createdAt = now - 2 * day, updatedAt = now - 2 * day,
            ),
            AvatarEntity(
                id = "avt_seed_002", name = "Living Room", source = "IMPORTED", status = "READY",
                splatCount = 800_000, fileBytes = 8_200_000L, format = "SPZ",
                capturedAt = now - 7 * day, captureDevice = null,
                estimatedHeightCm = null, posePreset = null, tags = "",
                locationLat = 35.6938, locationLng = 139.7034,
                locationName = "Shinjuku", locationAccuracyM = 8f,
                localFilePath = null, favorite = false,
                createdAt = now - 7 * day, updatedAt = now - 7 * day,
            ),
            AvatarEntity(
                id = "avt_seed_003", name = "Studio", source = "CAPTURED", status = "READY",
                splatCount = 920_000, fileBytes = 9_600_000L, format = "KSPLAT",
                capturedAt = now - 3 * day, captureDevice = "Pixel 8 Pro",
                estimatedHeightCm = null, posePreset = null, tags = "",
                locationLat = 35.6595, locationLng = 139.7005,
                locationName = "Shibuya", locationAccuracyM = 5f,
                localFilePath = null, favorite = false,
                createdAt = now - 3 * day, updatedAt = now - 3 * day,
            ),
            AvatarEntity(
                id = "avt_seed_004", name = "Garden Path", source = "CAPTURED", status = "READY",
                splatCount = 1_400_000, fileBytes = 14_200_000L, format = "PLY",
                capturedAt = now - 5 * day, captureDevice = "Pixel 8 Pro",
                estimatedHeightCm = null, posePreset = null, tags = "",
                locationLat = 35.4437, locationLng = 139.6380,
                locationName = "Yokohama", locationAccuracyM = 6f,
                localFilePath = null, favorite = false,
                createdAt = now - 5 * day, updatedAt = now - 5 * day,
            ),
            AvatarEntity(
                id = "avt_seed_005", name = "Mountain", source = "IMPORTED", status = "READY",
                splatCount = 2_100_000, fileBytes = 22_100_000L, format = "SPZ",
                capturedAt = now - 14 * day, captureDevice = null,
                estimatedHeightCm = null, posePreset = null, tags = "",
                locationLat = 35.2326, locationLng = 139.1069,
                locationName = "Hakone", locationAccuracyM = null,
                localFilePath = null, favorite = false,
                createdAt = now - 14 * day, updatedAt = now - 14 * day,
            ),
            AvatarEntity(
                id = "avt_seed_006", name = "Office", source = "IMPORTED", status = "READY",
                splatCount = 750_000, fileBytes = 7_800_000L, format = "PLY",
                capturedAt = now - 14 * day, captureDevice = null,
                estimatedHeightCm = null, posePreset = null, tags = "",
                locationLat = 35.7148, locationLng = 139.7967,
                locationName = "Asakusa", locationAccuracyM = null,
                localFilePath = null, favorite = false,
                createdAt = now - 14 * day, updatedAt = now - 14 * day,
            ),
        )
    }
}