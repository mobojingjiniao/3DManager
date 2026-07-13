package com.threed.manager.feature.avatars.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraX-backed capture pipeline for avatar creation.
 *
 * Wraps `ImageCapture` to produce JPEG files in the app's private cache,
 * optionally stamped with GPS coordinates via FusedLocationProvider.
 *
 * Phase 0 implements capture-only (no upload yet — see
 * [ConversionPipeline] for the FormScan client).
 */
class CameraSession(private val context: Context) {

    private val locationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val executor: Executor = ContextCompat.getMainExecutor(context)

    private var imageCapture: ImageCapture? = null

    /**
     * Bind a preview + image-capture use-case to the given lifecycle +
     * preview surface. Idempotent: re-binding will unbind first.
     */
    suspend fun bind(
        owner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    ) {
        val provider = ProcessCameraProvider.getInstance(context).await()
        provider.unbindAll()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        provider.bindToLifecycle(owner, selector, preview, capture)
        imageCapture = capture
    }

    /**
     * Capture one frame to [outFile] as JPEG. Optionally embed [location]
     * in the EXIF (caller is expected to have already requested
     * ACCESS_FINE_LOCATION permission).
     */
    suspend fun captureFrame(
        outFile: File,
        location: Location? = null,
    ): CaptureResult = withContext(Dispatchers.IO) {
        val capture = imageCapture ?: error("CameraSession.bind() must be called first")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outFile).apply {
            if (location != null) setMetadata(buildMetadata(location))
        }.build()

        suspendCancellableCoroutine<CaptureResult> { cont ->
            capture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                        val bytes = outFile.length()
                        cont.resume(CaptureResult(outFile.absolutePath, bytes, location))
                    }
                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                },
            )
        }
    }

    /** Read pixel dimensions of a JPEG without decoding the full image. */
    suspend fun readDimensions(file: File): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)
        opts.outWidth to opts.outHeight
    }

    /**
     * Read the latest GPS fix (best-effort). Returns null if location
     * permission is denied or no fix is available within [timeoutMs].
     */
    suspend fun currentLocation(timeoutMs: Long = 5_000L): Location? {
        if (!hasFineLocationPermission()) return null
        val cts = CancellationTokenSource()
        return try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine<Location?> { cont ->
                    locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                        .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
                }
            }
        } finally {
            cts.cancel()
        }
    }

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun buildMetadata(loc: Location): ImageCapture.Metadata {
        val md = ImageCapture.Metadata()
        md.location = loc
        return md
    }

    fun close() {
        imageCapture = null
    }

    data class CaptureResult(
        val path: String,
        val sizeBytes: Long,
        val location: Location?,
    )
}

// ── CompletableFuture / ListenableFuture helper ─────────────
private suspend fun <T> com.google.common.util.concurrent.ListenableFuture<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addListener(
            { try { cont.resume(get()) } catch (e: Exception) { cont.resumeWithException(e) } },
            { it.run() },
        )
    }