package com.threed.manager.feature.avatars.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Avatar repository interface.
 *
 * The app uses [com.threed.manager.feature.avatars.data.PersistentAvatarRepository]
 * via [AvatarRepositoryProvider] (Room-backed). This file holds the
 * default in-memory implementation used by previews / tests.
 */
interface AvatarRepository {
    val avatars: StateFlow<List<Avatar>>
    suspend fun getById(id: AvatarId): Avatar?
    fun observe(id: AvatarId): Flow<Avatar?>
    suspend fun toggleFavorite(id: AvatarId)
    suspend fun delete(id: AvatarId)
    suspend fun add(avatar: Avatar)
}

/** In-memory variant for previews and tests. */
open class InMemoryAvatarRepository : AvatarRepository {
    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    override val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()
    override suspend fun getById(id: AvatarId): Avatar? = _avatars.value.firstOrNull { it.id == id }
    override fun observe(id: AvatarId): Flow<Avatar?> = avatars.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun toggleFavorite(id: AvatarId) {
        _avatars.update { list ->
            list.map { if (it.id == id) it.copy(favorite = !it.favorite, updatedAt = System.currentTimeMillis()) else it }
        }
    }
    override suspend fun delete(id: AvatarId) { _avatars.update { list -> list.filterNot { it.id == id } } }
    override suspend fun add(avatar: Avatar) { _avatars.update { listOf(avatar) + it } }
}