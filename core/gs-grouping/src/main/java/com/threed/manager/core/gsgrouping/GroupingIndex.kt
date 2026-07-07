package com.threed.manager.core.gsgrouping

/**
 * O(1) read-optimized lookup index over a list of [GaussianGroup]s.
 *
 * Two lookups:
 *  - [groupOf]   : gaussian-id → containing group (first declared wins)
 *  - [group]     : group-id    → the group object itself
 *
 * Build cost: O(total Gaussian count across all groups). The build
 * is intended to run once per scene load, then read at render time.
 * Phase 4 will mmap-mirror this to disk for large scenes; the API
 * stays the same.
 *
 * Thread-safety: built-time is not concurrent. Once built, reads are
 * safe to call from any thread (the underlying [IntArray] / [Map] are
 * published via final-field semantics).
 */
class GroupingIndex private constructor(
    private val groupById: Map<String, GaussianGroup>,
    private val groupByGaussian: IntArray,
) {

    val groupCount: Int get() = groupById.size

    /**
     * Returns the group that contains the Gaussian with [gaussianId],
     * or `null` if it is not in any group.
     *
     * [gaussianId] is treated as a raw index into the global Gaussian
     * table — the index is sized to fit the largest seen id, and ids
     * above the size are reported as not-grouped (defensive: prevents
     * a stale index from throwing when the user removes Gaussians).
     */
    fun groupOf(gaussianId: Int): GaussianGroup? {
        if (gaussianId < 0 || gaussianId >= groupByGaussian.size) return null
        val groupId = groupByGaussian[gaussianId] - 1
        if (groupId < 0) return null
        // Map iteration on a small map is O(1) for our purposes.
        val id = groupIdByIndex[groupId] ?: return null
        return groupById[id]
    }

    fun group(groupId: String): GaussianGroup? = groupById[groupId]

    /** Index from group-index (0..groupCount-1) to its id. */
    private val groupIdByIndex: Array<String> = groupById.keys.toTypedArray()

    companion object {
        /**
         * Build a [GroupingIndex] from a list of [groups].
         *
         * Implementation: scan once to find the max `gaussianId` referenced
         * (so the lookup table is the right size), then a second pass to
         * populate. Storing `groupIndex + 1` so a `0` sentinel means
         * "not grouped".
         */
        fun build(groups: List<GaussianGroup>): GroupingIndex {
            if (groups.isEmpty()) {
                return GroupingIndex(emptyMap(), IntArray(0))
            }
            val maxId = groups.maxOf { g -> g.gaussianIndices.maxOrNull() ?: 0 }
            val lookup = IntArray(maxId + 1)  // 0 = not grouped
            for ((groupIndex, group) in groups.withIndex()) {
                val code = groupIndex + 1
                for (gaussianId in group.gaussianIndices) {
                    // First declared group wins (overlapping indices).
                    if (lookup[gaussianId] == 0) {
                        lookup[gaussianId] = code
                    }
                }
            }
            val map = groups.associateBy { it.id }
            return GroupingIndex(map, lookup)
        }
    }
}
