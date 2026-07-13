package com.threed.manager.feature.avatars

import android.content.Context
import com.threed.manager.feature.avatars.data.PersistentAvatarRepository
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.model.AvatarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single shared repository facade. Wires to the persistent Room-backed
 * implementation once an Application context is provided.
 *
 * Phase 0 uses [PersistentAvatarRepository]; future phases will swap to
 * a DI-injected repository.
 */
object AvatarRepositoryProvider {
    @Volatile private var instance: AvatarRepository? = null
    @Volatile private var persistent: PersistentAvatarRepository? = null

    fun init(context: Context) {
        if (instance != null) return
        synchronized(this) {
            if (instance != null) return
            val repo = PersistentAvatarRepository(context.applicationContext)
            persistent = repo
            instance = InMemoryAdapter(repo)
        }
        // Touch the field so kotlinc sees it as initialized.
        persistent
    }

    val repository: AvatarRepository
        get() = instance ?: error("AvatarRepositoryProvider.init(context) must be called first")

    val persistentRepository: PersistentAvatarRepository
        get() = persistent ?: error("AvatarRepositoryProvider.init(context) must be called first")

    /**
     * Adapter so the existing [AvatarRepository] interface continues to
     * work while persistence happens behind the scenes.
     */
    private class InMemoryAdapter(private val src: PersistentAvatarRepository) : AvatarRepository {
        override val avatars: StateFlow<List<Avatar>> = src.avatars
        override suspend fun getById(id: AvatarId): Avatar? = src.getById(id)
        override fun observe(id: AvatarId): Flow<Avatar?> = src.observe(id)
        override suspend fun toggleFavorite(id: AvatarId) = src.toggleFavorite(id)
        override suspend fun delete(id: AvatarId) = src.delete(id)
        override suspend fun add(avatar: Avatar) = src.add(avatar)
    }
}