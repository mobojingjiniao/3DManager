package com.threed.manager.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Client for the server-side `.ply` → `.ksplat` conversion pipeline.
 *
 * This is the mobile-side adapter for FormScan's conversion service
 * (or a self-hosted equivalent). Calls are async; the caller polls
 * [poll] for state updates until [ConversionState.Ready] (or a
 * terminal state) is observed.
 *
 * Phase 4: stub interface only. Production wires to FormScan's REST
 * API (see docs/architecture/API_CONTRACTS.md §2.1).
 */
interface AssetConversionApi {

    /** Submit a remote `.ply` for conversion. Returns the initial state. */
    suspend fun submit(request: ConversionRequest): SubmittedConversion

    /** Poll the conversion state for [conversionId] as a hot [Flow]. */
    fun poll(conversionId: String): Flow<ConversionState>

    /** Cancel an in-flight conversion. */
    suspend fun cancel(conversionId: String)
}

@Serializable
data class ConversionRequest(
    val sourceUrl: String,
    val targetSplatCount: Int = 500_000,
    val targetFormat: String = "ksplat",
    val priority: Int = 0,
)

@Serializable
data class SubmittedConversion(
    val conversionId: String,
    val state: ConversionState,
)

@Serializable
sealed interface ConversionState {
    val conversionId: String

    @Serializable
    data class Pending(override val conversionId: String) : ConversionState

    @Serializable
    data class Converting(override val conversionId: String, val progress: Float) : ConversionState {
        init {
            require(progress in 0f..1f) { "progress must be in [0, 1] but was $progress" }
        }
    }

    @Serializable
    data class Ready(override val conversionId: String, val downloadUrl: String) : ConversionState

    @Serializable
    data class Failed(override val conversionId: String, val reason: String) : ConversionState

    @Serializable
    data class Cancelled(override val conversionId: String) : ConversionState
}