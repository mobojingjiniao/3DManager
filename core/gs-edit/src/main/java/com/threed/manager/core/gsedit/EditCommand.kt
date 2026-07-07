package com.threed.manager.core.gsedit

import kotlinx.serialization.Serializable

/**
 * The five primitive 3DGS edit operations.
 *
 * Each command is self-describing (carries the parameters needed to
 * apply it) and self-inverse (carries enough info to undo without
 * consulting the prior state). The latter is the price for keeping
 * the in-memory [UndoRedoStack] light — disk persistence
 * (Phase 3.2 EditLogRepository) will record both directions
 * explicitly so a session can re-apply partial history.
 *
 * Operation semantics:
 *
 *  - [SetOpacity]   real-time, 60 fps. Adjusts α coefficient on a set
 *                   of high-Splat IDs.
 *
 *  - [SetColor]     real-time, 60 fps. Replaces the SH-0 (base color)
 *                   for the specified Splats.
 *
 *  - [Transform]    real-time, 60 fps. Applies a 4×4 matrix to the
 *                   selected Splats' positions. Only rigid transforms
 *                   (translate / rotate / uniform-scale) are allowed;
 *                   non-uniform scale would distort the Gaussian
 *                   ellipsoids and is rejected at apply time.
 *
 *  - [Prune]        offline (P1). Removes Splats from the rendered set.
 *                   Recorded as a "soft delete" — the layer system
 *                   (Phase 4) keeps the original data so undo can
 *                   restore them.
 *
 *  - [Relight]      semi-real-time (P2). Replaces the environment
 *                   map for the scene. Not a Splat-level operation
 *                   (one per scene, not per node).
 */
sealed interface EditCommand {

    val nodeIds: List<Int>

    @Serializable
    data class SetOpacity(
        override val nodeIds: List<Int>,
        val alpha: Float,
    ) : EditCommand

    @Serializable
    data class SetColor(
        override val nodeIds: List<Int>,
        val r: Float,
        val g: Float,
        val b: Float,
    ) : EditCommand

    @Serializable
    data class Transform(
        override val nodeIds: List<Int>,
        val matrix4x4: List<Float>,
    ) : EditCommand

    @Serializable
    data class Prune(
        override val nodeIds: List<Int>,
    ) : EditCommand

    @Serializable
    data class Relight(
        val envMapPath: String,
        val shDegree: Int = 2,
    ) : EditCommand {
        // Relight is scene-global, not Splat-local.
        override val nodeIds: List<Int> = emptyList()
    }
}
