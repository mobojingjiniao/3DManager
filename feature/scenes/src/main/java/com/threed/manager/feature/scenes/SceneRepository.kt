package com.threed.manager.feature.scenes

import com.threed.manager.core.model.SceneAsset
import kotlinx.coroutines.flow.Flow

/**
 * Source-of-truth abstraction for [SceneAsset] persistence.
 *
 * Production implementation is backed by Room (see core/data) and
 * decorates the result with [AssetConversionApi] lookups for
 * FormScan / Porin assets. Tests provide an in-memory implementation
 * that lives in this module's test sources.
 */
interface SceneRepository {
    suspend fun list(): List<SceneAsset>
    suspend fun upsert(asset: SceneAsset)
    suspend fun delete(id: String)
    fun observe(): Flow<List<SceneAsset>>
}