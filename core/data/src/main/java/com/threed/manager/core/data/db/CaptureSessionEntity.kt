package com.threed.manager.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Multi-view capture session — drives the AvatarCaptureScreen flow.
 *
 * One row per capture attempt; `frames` is a comma-separated list of
 * local JPEG paths; `state` mirrors CaptureState from the domain.
 */
@Entity(tableName = "capture_sessions")
data class CaptureSessionEntity(
    @PrimaryKey val id: String,
    val avatarId: String,
    val state: String,            // DRAFT / CAPTURING / UPLOADING / CONVERTING / READY / FAILED
    val framePaths: String,       // comma-separated JPEG paths
    val formScanJobId: String?,
    val progress: Float,
    val startedAt: Long,
    val finishedAt: Long?,
    val failureCode: String?,
    val failureMessage: String?,
)