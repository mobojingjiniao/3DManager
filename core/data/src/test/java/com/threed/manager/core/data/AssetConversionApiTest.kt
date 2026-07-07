package com.threed.manager.core.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Phase 4 — TDD RED tests for [AssetConversionApi].
 *
 * The API converts a remote `.ply` (potentially hundreds of MB) into a
 * compact `.ksplat` that the mobile app can stream. Three states:
 *  - [ConversionState.Pending]   queued, no worker started
 *  - [ConversionState.Converting] in progress (0 ≤ progress ≤ 100)
 *  - [ConversionState.Ready]      download URL available
 *  - [ConversionState.Failed]    retry exhausted
 *
 * RED at the moment of writing: `AssetConversionApi`, `ConversionState`,
 * and `ConversionRequest` are not yet declared. The interface is
 * designed to allow a mocked `FakeConversionApi` for tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AssetConversionApiTest {

    @Test
    fun `submit returns a conversionId`() = runTest {
        val api = FakeAssetConversionApi()
        val result = api.submit(ConversionRequest(sourceUrl = "https://api.local/v1/scenes/42"))
        assertThat(result.conversionId).isNotEmpty()
        assertThat(result.state).isInstanceOf(ConversionState.Pending::class.java)
    }

    @Test
    fun `poll returns Pending then Converting then Ready`() = runTest {
        val api = FakeAssetConversionApi().apply {
            // Override the simulated lifecycle to assert transitions deterministically.
            script = listOf(
                ConversionState.Pending("c-1"),
                ConversionState.Converting("c-1", 0.0f),
                ConversionState.Converting("c-1", 0.5f),
                ConversionState.Ready("c-1", downloadUrl = "https://cdn.local/c-1.ksplat"),
            )
        }
        val states: List<ConversionState> = api.poll("c-1").take(4).toList()
        assertThat(states[0]).isInstanceOf(ConversionState.Pending::class.java)
        assertThat(states[1]).isInstanceOf(ConversionState.Converting::class.java)
        assertThat(states[2]).isInstanceOf(ConversionState.Converting::class.java)
        assertThat(states[3]).isInstanceOf(ConversionState.Ready::class.java)
    }

    @Test
    fun `poll reaches Failed when script ends in failure`() = runTest {
        val api = FakeAssetConversionApi().apply {
            script = listOf(
                ConversionState.Pending("c-2"),
                ConversionState.Failed("c-2", "download error"),
            )
        }
        val states = api.poll("c-2").take(2).toList()
        assertThat(states[1]).isInstanceOf(ConversionState.Failed::class.java)
        assertThat((states[1] as ConversionState.Failed).reason).contains("download")
    }

    @Test
    fun `cancel transitions a Pending or Converting job to Cancelled`() = runTest {
        val api = FakeAssetConversionApi().apply {
            script = listOf(ConversionState.Converting("c-3", 0.2f))
        }
        val first = api.poll("c-3").take(1).toList().first()
        assertThat(first).isInstanceOf(ConversionState.Converting::class.java)
        api.cancel("c-3")
        // After cancel, polling emits script + a terminal Cancelled.
        val after = api.poll("c-3").toList().last()
        assertThat(after).isInstanceOf(ConversionState.Cancelled::class.java)
    }
}