package com.threed.manager.feature.avatars.upload

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Polls a FormScan conversion job until it reaches a terminal state
 * (READY or FAILED). Backoff cadence: 3s → 6s → 12s (capped).
 *
 * Emits [Progress] updates on every poll so the UI can show
 * live progress + ETA.
 */
class ConversionPoller(private val api: FormScanApi) {

    data class Progress(
        val state: String,
        val percent: Float,
        val splatCount: Int?,
        val outputUrl: String?,
    )

    sealed class Outcome {
        data class Ready(val progress: Progress) : Outcome()
        data class Failed(val reason: String) : Outcome()
    }

    /**
     * @param jobId The conversion ID returned by [FormScanApi.submit].
     * @param maxAttempts Safety cap (default 20 → ~4 minutes at 12s cap).
     */
    fun pollUntilTerminal(jobId: String, maxAttempts: Int = 20): Flow<Progress> = flow {
        var attempt = 0
        var backoffMs = 3_000L
        while (attempt < maxAttempts) {
            attempt++
            when (val r = api.poll(jobId)) {
                is FormScanApi.Result.Ok -> {
                    val p = r.value
                    val prog = Progress(p.state, p.progress, p.splatCount, p.outputUrl)
                    emit(prog)
                    if (p.state == "READY" || p.state == "FAILED") return@flow
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(12_000L)
                }
                is FormScanApi.Result.Failed -> {
                    Log.w(TAG, "poll failed: ${r.reason}")
                    delay(backoffMs)
                }
            }
        }
    }

    /**
     * Convenience collector that awaits terminal state, surfacing
     * the final outcome (Ready / Failed) once reached.
     */
    suspend fun awaitTerminal(jobId: String): Outcome {
        var last: Progress? = null
        try {
            pollUntilTerminal(jobId).collect { last = it }
        } catch (e: CancellationException) {
            throw e
        }
        return when (last?.state) {
            "READY"  -> Outcome.Ready(last!!)
            "FAILED" -> Outcome.Failed("Conversion reported FAILED")
            else     -> Outcome.Failed("Conversion timed out")
        }
    }

    private companion object { const val TAG = "ConversionPoller" }
}