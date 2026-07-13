package com.threed.manager.feature.avatars.upload

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal FormScan REST client.
 *
 * Phase 0 ships with an offline simulator (offlineSimulate = true) that
 * ramps job progress deterministically so the full pipeline can be
 * tested without a live backend. Switch to a real endpoint by setting
 * baseUrl to an https URL and offlineSimulate = false.
 */
class FormScanApi(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val authToken: String? = null,
    /** When true, bypass network entirely. Set during local dev. */
    private val offlineSimulate: Boolean = true,
) {

    sealed class Result<out T> {
        data class Ok<T>(val value: T) : Result<T>()
        data class Failed(val reason: String) : Result<Nothing>()
    }

    data class SubmitResponse(
        val jobId: String,
        val acceptedFrames: Int,
        val estimatedSeconds: Int,
    )

    data class JobStatus(
        val jobId: String,
        val state: String,           // PENDING / RUNNING / READY / FAILED
        val progress: Float,
        val splatCount: Int?,
        val outputUrl: String?,
    )

    suspend fun submit(
        frames: List<File>,
        meta: Map<String, String> = emptyMap(),
    ): Result<SubmitResponse> = withContext(Dispatchers.IO) {
        if (offlineSimulate || !baseUrl.startsWith("http")) {
            delay(120)
            return@withContext Result.Ok(
                SubmitResponse(
                    jobId = "fsk_${System.currentTimeMillis().toString(36)}",
                    acceptedFrames = frames.size,
                    estimatedSeconds = 30,
                ),
            )
        }
        try {
            val boundary = "----3DManager-${System.nanoTime()}"
            val url = URL("$baseUrl/api/v1/convert")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                connectTimeout = 15_000
                readTimeout = 60_000
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
            }
            conn.outputStream.use { out ->
                frames.forEach { f ->
                    writePart(out, boundary, "frames", f.name, "image/jpeg", f)
                }
                writeJsonPart(out, boundary, "meta", meta)
                out.write("--$boundary--\r\n".toByteArray())
                out.flush()
            }
            val code = conn.responseCode
            if (code !in 200..299) return@withContext Result.Failed("HTTP $code")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            // Lightweight field extraction to avoid pulling org.json.
            val jobId = "\"job_id\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(body)?.groupValues?.get(1)
                ?: "fsk_unknown"
            Result.Ok(
                SubmitResponse(
                    jobId = jobId,
                    acceptedFrames = frames.size,
                    estimatedSeconds = 30,
                ),
            )
        } catch (e: IOException) {
            Log.w(TAG, "submit() network error: ${e.message}")
            Result.Failed("network: ${e.message}")
        }
    }

    suspend fun poll(jobId: String): Result<JobStatus> = withContext(Dispatchers.IO) {
        if (offlineSimulate || !baseUrl.startsWith("http")) {
            val current = localProgress[jobId] ?: 0.25f
            val next = (current + 0.25f).coerceAtMost(1f)
            localProgress[jobId] = next
            delay(200)
            return@withContext Result.Ok(
                JobStatus(
                    jobId = jobId,
                    state = when {
                        next >= 1f -> "READY"
                        next > 0.5f -> "RUNNING"
                        else -> "PENDING"
                    },
                    progress = next,
                    splatCount = if (next >= 1f) 1_000_000 else null,
                    outputUrl = if (next >= 1f) "local://cache/$jobId.ksplat" else null,
                ),
            )
        }
        try {
            val url = URL("$baseUrl/api/v1/convert/$jobId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                authToken?.let { setRequestProperty("Authorization", "Bearer $it") }
            }
            if (conn.responseCode !in 200..299) return@withContext Result.Failed("HTTP ${conn.responseCode}")
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val state = "\"state\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)?.groupValues?.get(1) ?: "UNKNOWN"
            val progress = "\"progress\"\\s*:\\s*([0-9.]+)".toRegex().find(json)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
            val splat = "\"splat_count\"\\s*:\\s*([0-9]+)".toRegex().find(json)?.groupValues?.get(1)?.toIntOrNull()
            val output = "\"output_url\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)?.groupValues?.get(1)
            Result.Ok(JobStatus(jobId, state, progress, splat, output))
        } catch (e: IOException) {
            Log.w(TAG, "poll() network error: ${e.message}")
            Result.Failed("network: ${e.message}")
        }
    }

    private fun writePart(
        out: OutputStream,
        boundary: String,
        name: String,
        filename: String,
        contentType: String,
        file: File,
    ) {
        out.write("--$boundary\r\n".toByteArray())
        out.write("Content-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n".toByteArray())
        out.write("Content-Type: $contentType\r\n\r\n".toByteArray())
        FileInputStream(file).use { it.copyTo(out) }
        out.write("\r\n".toByteArray())
    }

    private fun writeJsonPart(out: OutputStream, boundary: String, name: String, meta: Map<String, String>) {
        val json = meta.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":\"${it.value}\"" }
        out.write("--$boundary\r\n".toByteArray())
        out.write("Content-Disposition: form-data; name=\"$name\"\r\n".toByteArray())
        out.write("Content-Type: application/json\r\n\r\n".toByteArray())
        out.write(json.toByteArray())
        out.write("\r\n".toByteArray())
    }

    private companion object {
        const val TAG = "FormScanApi"
        const val DEFAULT_BASE_URL = "https://formscan.local"  // dev placeholder
        val localProgress = mutableMapOf<String, Float>()
    }
}