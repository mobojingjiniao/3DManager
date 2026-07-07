package com.threed.manager.feature.scenes

import com.threed.manager.core.data.AssetConversionApi
import com.threed.manager.core.data.ConversionRequest
import com.threed.manager.core.data.ConversionState
import com.threed.manager.core.data.SubmittedConversion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Local test double for [AssetConversionApi] — declared in the feature
 * module's test sources because the production-grade Fake (with script
 * support) lives in core/data's test sources and is not exported across
 * module boundaries.
 */
internal class FakeAssetConversionApi(
    var script: List<ConversionState> = listOf(
        ConversionState.Pending("c-fake"),
        ConversionState.Converting("c-fake", 0.5f),
        ConversionState.Ready("c-fake", "https://cdn.local/c-fake.ksplat"),
    ),
) : AssetConversionApi {

    private var counter = 0

    override suspend fun submit(request: ConversionRequest): SubmittedConversion {
        val id = "c-${++counter}"
        return SubmittedConversion(id, ConversionState.Pending(id))
    }

    override fun poll(conversionId: String): Flow<ConversionState> = flowOf(*script.toTypedArray())

    override suspend fun cancel(conversionId: String) = Unit
}