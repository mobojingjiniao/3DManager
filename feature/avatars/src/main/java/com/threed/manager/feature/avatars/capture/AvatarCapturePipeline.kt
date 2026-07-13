package com.threed.manager.feature.avatars.capture

import android.content.Context
import android.location.Location
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.threed.manager.core.data.db.AvatarDao
import com.threed.manager.core.data.db.AvatarEntity
import com.threed.manager.core.data.db.CaptureSessionDao
import com.threed.manager.core.data.db.CaptureSessionEntity
import com.threed.manager.core.data.db.ThreeDManagerDatabase
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.model.AvatarSource
import com.threed.manager.feature.avatars.model.AvatarStatus
import com.threed.manager.feature.avatars.model.SplatFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Multi-step avatar capture pipeline.
 *
 * Steps:
 *   1. Bind camera (CameraSession.bind).
 *   2. Capture N frames, each tagged with GPS when available.
 *   3. Persist [CaptureSessionEntity] in Room.
 *   4. Submit to FormScan (Phase 4 stub for now).
 *   5. Poll conversion; on READY, write a new AvatarEntity.
 *
 * Exposes a [state] StateFlow for the UI to bind against.
 */
class AvatarCapturePipeline(private val context: Context) {

    private val session = CameraSession(context)
    private val avatarDao: AvatarDao = ThreeDManagerDatabase.get(context).avatarDao()
    private val captureDao: CaptureSessionDao = ThreeDManagerDatabase.get(context).captureSessionDao()

    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    suspend fun bindCamera(owner: LifecycleOwner, previewView: PreviewView) {
        session.bind(owner, previewView)
    }

    suspend fun captureOne(targetFrames: Int = 24): String = withContext(Dispatchers.IO) {
        val current = _state.value
        if (current.framePaths.size >= targetFrames) return@withContext current.id

        val location = session.currentLocation()
        val framesDir = File(context.cacheDir, "captures/${current.id}").apply { mkdirs() }
        val outFile = File(framesDir, "frame_${current.framePaths.size.coerceAtLeast(0).toString().padStart(3, '0')}.jpg")
        val result = session.captureFrame(outFile, location)
        val newPaths = current.framePaths + result.path
        val progress = newPaths.size.toFloat() / targetFrames
        _state.update {
            it.copy(
                framePaths = newPaths,
                progress = progress,
                stage = CaptureStage.CAPTURING,
                lastLocation = location ?: it.lastLocation,
            )
        }
        // Persist progress to Room so the session survives a process kill.
        captureDao.upsert(buildSessionEntity(targetFrames).copy(
            framePaths = newPaths.joinToString(","),
            progress = progress,
            state = CaptureStage.CAPTURING.name,
        ))
        outFile.absolutePath
    }

    /** Move into CONVERTING phase. Calls the (stub) FormScan pipeline. */
    suspend fun submit(name: String, targetSplatCount: Int = 1_000_000): AvatarId = withContext(Dispatchers.IO) {
        val current = _state.value
        _state.update { it.copy(stage = CaptureStage.UPLOADING) }
        captureDao.upsert(buildSessionEntity(current.framePaths.size).copy(state = CaptureStage.UPLOADING.name))

        // Phase 0 stub: simulate an upload + conversion locally.
        val resultSplat = ConversionResult(
            avatarName = name,
            splatCount = targetSplatCount,
            fileBytes = 12_400_000L,
            format = SplatFormat.KSPLAT,
            location = current.lastLocation,
        )

        _state.update { it.copy(stage = CaptureStage.CONVERTING) }
        captureDao.upsert(buildSessionEntity(current.framePaths.size).copy(state = CaptureStage.CONVERTING.name))

        val id = AvatarId("avt_${UUID.randomUUID().toString().take(8)}")
        val now = System.currentTimeMillis()
        avatarDao.upsert(
            AvatarEntity(
                id = id.raw,
                name = resultSplat.avatarName,
                source = AvatarSource.CAPTURED.name,
                status = AvatarStatus.READY.name,
                splatCount = resultSplat.splatCount.toLong(),
                fileBytes = resultSplat.fileBytes,
                format = resultSplat.format.name,
                capturedAt = now,
                captureDevice = android.os.Build.MODEL,
                estimatedHeightCm = null,
                posePreset = "idle",
                tags = "",
                locationLat = resultSplat.location?.latitude,
                locationLng = resultSplat.location?.longitude,
                locationName = null,
                locationAccuracyM = resultSplat.location?.accuracy,
                localFilePath = null,
                favorite = false,
                createdAt = now,
                updatedAt = now,
            ),
        )

        _state.update {
            it.copy(
                stage = CaptureStage.COMPLETE,
                progress = 1f,
                resultAvatarId = id,
            )
        }
        captureDao.upsert(buildSessionEntity(current.framePaths.size).copy(
            state = CaptureStage.COMPLETE.name,
            finishedAt = now,
            progress = 1f,
        ))
        id
    }

    private fun buildSessionEntity(targetFrames: Int): CaptureSessionEntity {
        val current = _state.value
        return CaptureSessionEntity(
            id = current.id,
            avatarId = current.resultAvatarId?.raw ?: "",
            state = current.stage.name,
            framePaths = current.framePaths.joinToString(","),
            formScanJobId = null,
            progress = current.progress,
            startedAt = current.startedAt,
            finishedAt = if (current.stage == CaptureStage.COMPLETE) System.currentTimeMillis() else null,
            failureCode = null,
            failureMessage = null,
        ).also {
            // Persist target frames implicitly via metadata; left out of
            // entity for simplicity (callers know it).
            @Suppress("UNUSED_VARIABLE") val _t = targetFrames
        }
    }

    private fun framesDir(session: CameraSession): File =
        File(context.cacheDir, "captures/${session.let { _state.value.id }}").apply { mkdirs() }

    companion object {
        /** Build a pipeline with the standard frame cache location. */
        fun create(context: Context): AvatarCapturePipeline =
            AvatarCapturePipeline(context)
    }
}

/** Live state for the capture pipeline. */
data class CaptureState(
    val id: String = "cap_${UUID.randomUUID().toString().take(8)}",
    val stage: CaptureStage = CaptureStage.IDLE,
    val framePaths: List<String> = emptyList(),
    val progress: Float = 0f,
    val lastLocation: Location? = null,
    val resultAvatarId: AvatarId? = null,
    val startedAt: Long = System.currentTimeMillis(),
)

enum class CaptureStage {
    IDLE, CAPTURING, UPLOADING, CONVERTING, COMPLETE, FAILED
}

/** Result of the conversion stub. Real impl populated by FormScan. */
data class ConversionResult(
    val avatarName: String,
    val splatCount: Int,
    val fileBytes: Long,
    val format: SplatFormat,
    val location: Location?,
)