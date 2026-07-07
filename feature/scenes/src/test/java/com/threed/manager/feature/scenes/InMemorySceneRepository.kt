package com.threed.manager.feature.scenes

import com.threed.manager.core.model.SceneAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory repository used by SceneListViewModel tests. Mirrors the
 * production SceneRepository contract so tests can drive the view
 * model without spinning up Room.
 */
internal class InMemorySceneRepository : SceneRepository {
    private val store = mutableListOf<SceneAsset>()
    private val flow = MutableStateFlow<List<SceneAsset>>(emptyList())

    fun seed(asset: SceneAsset) {
        store += asset
        flow.value = store.toList()
    }

    override suspend fun list(): List<SceneAsset> = store.toList()

    override suspend fun upsert(asset: SceneAsset) {
        store.removeAll { it.id == asset.id }
        store += asset
        flow.value = store.toList()
    }

    override suspend fun delete(id: String) {
        store.removeAll { it.id == id }
        flow.value = store.toList()
    }

    override fun observe(): Flow<List<SceneAsset>> = flow
}