package com.threed.manager.feature.scenes

import com.google.common.truth.Truth.assertThat
import com.threed.manager.core.data.ConversionRequest
import com.threed.manager.core.data.ConversionState
import com.threed.manager.core.data.AssetConversionApi
import com.threed.manager.core.model.SceneAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Phase 4 — TDD RED tests for [SceneListViewModel].
 *
 * The view model exposes a hot [StateFlow] of [SceneListState] and
 * dispatches user intents ([refresh], [importLocal], [requestConversion]).
 * The actual network calls are stubbed via [FakeAssetConversionApi].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SceneListViewModelTest {

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val vm = SceneListViewModel(repo = InMemorySceneRepository(), api = FakeAssetConversionApi())
        assertThat(vm.state.value.scenes).isEmpty()
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test
    fun `refresh loads scenes from repository`() = runTest {
        val repo = InMemorySceneRepository().apply {
            seed(SceneAsset(id = "1", name = "Living Room", splatCount = 250_000, sourcePath = "/sdcard/lr.ksplat"))
            seed(SceneAsset(id = "2", name = "Garden", splatCount = 1_200_000, sourcePath = "/sdcard/garden.ksplat"))
        }
        val vm = SceneListViewModel(repo = repo, api = FakeAssetConversionApi())
        vm.refresh()
        assertThat(vm.state.value.scenes).hasSize(2)
    }

    @Test
    fun `importLocal adds a scene to the list`() = runTest {
        val repo = InMemorySceneRepository()
        val vm = SceneListViewModel(repo = repo, api = FakeAssetConversionApi())
        vm.importLocal(SceneAsset(id = "x", name = "Imported", splatCount = 100, sourcePath = "/sdcard/x.ksplat"))
        assertThat(vm.state.value.scenes).hasSize(1)
        assertThat(vm.state.value.scenes.first().name).isEqualTo("Imported")
    }

    @Test
    fun `requestConversion transitions through Pending → Converting → Ready`() = runTest {
        val api = FakeAssetConversionApi().apply {
            script = listOf(
                ConversionState.Pending("c-1"),
                ConversionState.Converting("c-1", 0.5f),
                ConversionState.Ready("c-1", downloadUrl = "https://cdn.local/c-1.ksplat"),
            )
        }
        val vm = SceneListViewModel(repo = InMemorySceneRepository(), api = api)
        val finalState = vm.requestConversion(
            ConversionRequest(sourceUrl = "https://api.local/v1/scenes/42"),
        )
        assertThat(finalState).isInstanceOf(ConversionState.Ready::class.java)
        val downloads = vm.state.value.pendingDownloads
        assertThat(downloads).containsKey("c-1")
    }

    @Test
    fun `importLocal with very large asset emits warning`() = runTest {
        val repo = InMemorySceneRepository()
        val vm = SceneListViewModel(repo = repo, api = FakeAssetConversionApi())
        // 5M splats exceeds the typical HIGH tier budget (> 500k static)
        vm.importLocal(SceneAsset(id = "big", name = "Mansion", splatCount = 5_000_000, sourcePath = "/sdcard/big.ksplat"))
        assertThat(vm.state.value.warnings).isNotEmpty()
        assertThat(vm.state.value.warnings.first()).contains("oversized")
    }
}