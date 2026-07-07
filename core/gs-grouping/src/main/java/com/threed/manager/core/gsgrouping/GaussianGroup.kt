package com.threed.manager.core.gsgrouping

import kotlinx.serialization.Serializable

/**
 * A semantic grouping of Gaussians, typically produced by an offline
 * GaussianGrouping pass (e.g. SAM-based) and shipped as a sidecar file
 * alongside the .ksplat asset.
 *
 * IDs are stable across sessions (assigned by the upstream tool, not
 * derived from indices). The label is what the user sees in the editor.
 *
 * Memory: `gaussianIndices` is large (one int per Gaussian in the
 * group). For a 1M-splat scene with 100 groups averaging 10k splats
 * each, expect ~4 MB raw. Phase 4 will move this to disk-backed
 * storage with mmap; the in-memory representation stays as
 * [IntArray] for the warm path.
 */
@Serializable
data class GaussianGroup(
    val id: String,
    val label: String,
    val gaussianIndices: IntArray,
) {
    init {
        require(id.isNotBlank()) { "GaussianGroup id must not be blank" }
        require(label.isNotBlank()) { "GaussianGroup label must not be blank" }
        require(gaussianIndices.isNotEmpty()) { "GaussianGroup must reference at least one Gaussian" }
    }

    // Generated equals/hashCode because [IntArray] does not provide a value
    // based one.
    override fun equals(other: Any?): Boolean = other is GaussianGroup &&
        id == other.id &&
        label == other.label &&
        gaussianIndices.contentEquals(other.gaussianIndices)

    override fun hashCode(): Int = 31 * id.hashCode() + gaussianIndices.contentHashCode()
}
