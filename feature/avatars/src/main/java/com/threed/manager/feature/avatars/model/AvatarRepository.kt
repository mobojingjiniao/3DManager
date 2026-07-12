package com.threed.manager.feature.avatars.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory avatar repository. Phase 0 ships with a seed of 6 avatars
 * so the app has real content on first launch. Real Room-backed
 * repository will replace this in Phase 2.
 */
class AvatarRepository {
    private val _avatars = MutableStateFlow(seed())
    val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()

    fun getById(id: AvatarId): Avatar? = _avatars.value.firstOrNull { it.id == id }

    fun toggleFavorite(id: AvatarId) {
        _avatars.update { list ->
            list.map { if (it.id == id) it.copy(favorite = !it.favorite, updatedAt = System.currentTimeMillis()) else it }
        }
    }

    fun delete(id: AvatarId) {
        _avatars.update { list -> list.filterNot { it.id == id } }
    }

    fun add(avatar: Avatar) {
        _avatars.update { listOf(avatar) + it }
    }

    private fun seed(): List<Avatar> {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        return listOf(
            Avatar(
                id = AvatarId("avt_seed_001"),
                name = "Me",
                source = AvatarSource.CAPTURED,
                status = AvatarStatus.READY,
                favorite = true,
                metadata = AvatarMetadata(
                    splatCount = 1_247_392,
                    fileBytes = 12_400_000L,
                    format = SplatFormat.KSPLAT,
                    capturedAt = now - 2 * day,
                    captureDevice = "Pixel 8 Pro",
                    estimatedHeightCm = 1.7f,
                    posePreset = "idle",
                    locationLat = 35.6762,
                    locationLng = 139.6503,
                    locationName = "Tokyo, JP",
                    locationAccuracyM = 4f,
                ),
                createdAt = now - 2 * day,
                updatedAt = now - 2 * day,
            ),
            Avatar(
                id = AvatarId("avt_seed_002"),
                name = "Living Room",
                source = AvatarSource.IMPORTED,
                status = AvatarStatus.READY,
                metadata = AvatarMetadata(
                    splatCount = 800_000,
                    fileBytes = 8_200_000L,
                    format = SplatFormat.SPZ,
                    capturedAt = now - 7 * day,
                    locationLat = 35.6938,
                    locationLng = 139.7034,
                    locationName = "Shinjuku",
                    locationAccuracyM = 8f,
                ),
                createdAt = now - 7 * day,
            ),
            Avatar(
                id = AvatarId("avt_seed_003"),
                name = "Studio",
                source = AvatarSource.CAPTURED,
                status = AvatarStatus.READY,
                metadata = AvatarMetadata(
                    splatCount = 920_000,
                    fileBytes = 9_600_000L,
                    format = SplatFormat.KSPLAT,
                    capturedAt = now - 3 * day,
                    captureDevice = "Pixel 8 Pro",
                    locationLat = 35.6595,
                    locationLng = 139.7005,
                    locationName = "Shibuya",
                    locationAccuracyM = 5f,
                ),
                createdAt = now - 3 * day,
            ),
            Avatar(
                id = AvatarId("avt_seed_004"),
                name = "Garden Path",
                source = AvatarSource.CAPTURED,
                status = AvatarStatus.READY,
                metadata = AvatarMetadata(
                    splatCount = 1_400_000,
                    fileBytes = 14_200_000L,
                    format = SplatFormat.PLY,
                    capturedAt = now - 5 * day,
                    locationLat = 35.4437,
                    locationLng = 139.6380,
                    locationName = "Yokohama",
                    locationAccuracyM = 6f,
                ),
                createdAt = now - 5 * day,
            ),
            Avatar(
                id = AvatarId("avt_seed_005"),
                name = "Mountain",
                source = AvatarSource.IMPORTED,
                status = AvatarStatus.READY,
                metadata = AvatarMetadata(
                    splatCount = 2_100_000,
                    fileBytes = 22_100_000L,
                    format = SplatFormat.SPZ,
                    capturedAt = now - 14 * day,
                    locationLat = 35.2326,
                    locationLng = 139.1069,
                    locationName = "Hakone",
                ),
            ),
            Avatar(
                id = AvatarId("avt_seed_006"),
                name = "Office",
                source = AvatarSource.IMPORTED,
                status = AvatarStatus.READY,
                metadata = AvatarMetadata(
                    splatCount = 750_000,
                    fileBytes = 7_800_000L,
                    format = SplatFormat.PLY,
                    capturedAt = now - 14 * day,
                    locationLat = 35.7148,
                    locationLng = 139.7967,
                    locationName = "Asakusa",
                ),
            ),
        )
    }
}