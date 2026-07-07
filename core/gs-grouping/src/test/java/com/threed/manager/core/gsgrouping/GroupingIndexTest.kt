package com.threed.manager.core.gsgrouping

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 3.2 — TDD RED tests for [GaussianGroup] and [GroupingIndex].
 *
 * The index is a read-optimized cache: O(1) lookup from a single
 * Gaussian ID to the group it belongs to, plus O(1) group-by-id
 * lookup. It is built once at scene-load time (or after GaussianGrouping
 * runs in the cloud) and read at render time when the user picks a
 * Gaussian.
 *
 * The screen-space selector is intentionally not covered here — it
 * depends on a camera-projection abstraction that lands in Phase 3.3.
 */
class GroupingIndexTest {

    @Test
    fun `build with no groups is empty`() {
        val index = GroupingIndex.build(emptyList())
        assertThat(index.groupCount).isEqualTo(0)
        assertThat(index.groupOf(gaussianId = 0)).isNull()
        assertThat(index.group(groupId = "any")).isNull()
    }

    @Test
    fun `build records each Gaussian to its group for O(1) lookup`() {
        val groups = listOf(
            GaussianGroup(id = "tree", label = "Tree 3", gaussianIndices = intArrayOf(10, 11, 12, 13)),
            GaussianGroup(id = "ground", label = "Ground", gaussianIndices = intArrayOf(100, 101, 200)),
        )
        val index = GroupingIndex.build(groups)
        assertThat(index.groupCount).isEqualTo(2)
        assertThat(index.groupOf(10)?.id).isEqualTo("tree")
        assertThat(index.groupOf(11)?.id).isEqualTo("tree")
        assertThat(index.groupOf(100)?.id).isEqualTo("ground")
        assertThat(index.groupOf(200)?.id).isEqualTo("ground")
        // 42 is not in any group
        assertThat(index.groupOf(42)).isNull()
    }

    @Test
    fun `overlapping indices are assigned to first-declared group`() {
        // Real GaussianGrouping output can put a single Gaussian in
        // multiple candidate groups. We pick the first declaration as
        // the canonical owner to keep the index a function rather than
        // a multi-map; UI shows "primary group" in this case.
        val groups = listOf(
            GaussianGroup(id = "a", label = "A", gaussianIndices = intArrayOf(1, 2, 3)),
            GaussianGroup(id = "b", label = "B", gaussianIndices = intArrayOf(2, 4)),
        )
        val index = GroupingIndex.build(groups)
        assertThat(index.groupOf(2)?.id).isEqualTo("a")
    }

    @Test
    fun `group by id returns the full GaussianGroup object`() {
        val groups = listOf(
            GaussianGroup(id = "tree", label = "Tree", gaussianIndices = intArrayOf(10, 11)),
        )
        val index = GroupingIndex.build(groups)
        val retrieved = index.group("tree")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.label).isEqualTo("Tree")
        assertThat(retrieved?.gaussianIndices?.toList()).containsExactly(10, 11).inOrder()
    }
}
