package com.threed.manager.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Local test double for [AssetConversionApi].
 *
 * The caller controls the simulated state machine by setting [script];
 * [poll] re-emits those states in order. Useful for both unit tests
 * and for Compose previews that need to drive the conversion UI without
 * hitting a real backend.
 */
internal class FakeAssetConversionApi : AssetConversionApi {

    /** Scripted state sequence. Override before each test. */
    var script: List<ConversionState> = listOf(
        ConversionState.Pending("c-fake"),
        ConversionState.Converting("c-fake", 0.0f),
        ConversionState.Converting("c-fake", 0.5f),
        ConversionState.Ready("c-fake", "https://cdn.local/c-fake.ksplat"),
    )

    private var counter = 0
    private val cancelled = mutableSetOf<String>()

    override suspend fun submit(request: ConversionRequest): SubmittedConversion {
        val id = "c-${++counter}"
        return SubmittedConversion(id, ConversionState.Pending(id))
    }

    override fun poll(conversionId: String): Flow<ConversionState> {
        val emitted = if (conversionId in cancelled) {
            script + ConversionState.Cancelled(conversionId)
        } else {
            script
        }
        return flowOf(*emitted.toTypedArray())
    }

    override suspend fun cancel(conversionId: String) {
        cancelled += conversionId
    }
}