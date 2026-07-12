package com.threed.manager.feature.avatars.model

import kotlinx.serialization.Serializable

/**
 * Avatar domain model for the v3.2 Cinematic Dark Aurora design.
 *
 * Mirrors the master-plan contract with the addition of GPS / location fields
 * for the Porin Cloud-style map view.
 */
@Serializable
data class Avatar(
    val id: AvatarId,
    val name: String,
    val source: AvatarSource,
    val status: AvatarStatus,
    val metadata: AvatarMetadata,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
@JvmInline
value class AvatarId(val raw: String) {
    companion object {
        fun new(): AvatarId = AvatarId("avt_${System.nanoTime().toString(36)}")
    }
}

@Serializable
enum class AvatarSource { CAPTURED, IMPORTED, GENERATED, SAMPLE }

@Serializable
enum class AvatarStatus { DRAFT, READY, SYNCING, FAILED }

@Serializable
data class AvatarMetadata(
    val splatCount: Long,
    val fileBytes: Long,
    val format: SplatFormat,
    val capturedAt: Long? = null,
    val captureDevice: String? = null,
    val estimatedHeightCm: Float? = null,
    val posePreset: String? = null,
    val tags: List<String> = emptyList(),
    // GPS / location (Porin Cloud-style)
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationName: String? = null,
    val locationAccuracyM: Float? = null,
) {
    val hasLocation: Boolean
        get() = locationLat != null && locationLng != null
}

@Serializable
enum class SplatFormat(val extension: String) {
    PLY("ply"),
    SPLAT("splat"),
    KSPLAT("ksplat"),
    SPZ("spz"),
    SOG("sog"),
    GLB("glb"),
}

@Serializable
data class AvatarPose(
    val id: String,
    val displayName: String,
    val keyframes: List<PoseKeyframe>,
    val durationMs: Int,
    val loop: Boolean = true,
)

@Serializable
data class PoseKeyframe(
    val timeMs: Int,
    val yawDeg: Float,
    val pitchDeg: Float,
    val distanceM: Float,
)

/** Named pose presets used by the pose picker on the detail screen. */
object PosePresets {
    val Idle     = AvatarPose("idle", "idle", listOf(PoseKeyframe(0, 0f, 0f, 1.5f)), 0, true)
    val Wave     = AvatarPose("wave", "wave", listOf(PoseKeyframe(0, 25f, -5f, 1.5f), PoseKeyframe(400, 25f, -5f, 1.5f)), 800, true)
    val Bow      = AvatarPose("bow", "bow", listOf(PoseKeyframe(0, 0f, 20f, 1.2f)), 600, true)
    val ThumbsUp = AvatarPose("thumbsup", "thumbs", listOf(PoseKeyframe(0, -10f, -10f, 1.3f)), 700, true)
    val Cheer    = AvatarPose("cheer", "cheer", listOf(PoseKeyframe(0, 0f, -15f, 1.6f)), 900, true)
    val Salute   = AvatarPose("salute", "salute", listOf(PoseKeyframe(0, 0f, -5f, 1.4f)), 600, true)
    val Clap     = AvatarPose("clap", "clap", listOf(PoseKeyframe(0, 0f, 0f, 1.5f), PoseKeyframe(300, 0f, 0f, 1.5f)), 600, true)
    val Point    = AvatarPose("point", "point", listOf(PoseKeyframe(0, 15f, -5f, 1.5f)), 500, true)
    val Stretch  = AvatarPose("stretch", "stretch", listOf(PoseKeyframe(0, 0f, -25f, 1.4f)), 700, true)
    val Spin     = AvatarPose("spin", "spin", listOf(PoseKeyframe(0, 0f, 0f, 1.5f), PoseKeyframe(2000, 360f, 0f, 1.5f)), 2000, true)
    val Kneel    = AvatarPose("kneel", "kneel", listOf(PoseKeyframe(0, 0f, 30f, 1.0f)), 800, true)
    val Jump     = AvatarPose("jump", "jump", listOf(PoseKeyframe(0, 0f, -15f, 1.6f)), 600, true)

    val all: List<AvatarPose> = listOf(Idle, Wave, Bow, ThumbsUp, Cheer, Salute, Clap, Point, Stretch, Spin, Kneel, Jump)
}