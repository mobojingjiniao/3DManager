package com.threed.manager.feature.scenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threed.manager.core.data.AssetConversionApi
import com.threed.manager.core.data.ConversionRequest
import com.threed.manager.core.data.ConversionState
import com.threed.manager.core.data.SubmittedConversion
import com.threed.manager.core.model.SceneAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * View model for the scene-list screen.
 *
 * Inputs (intents):
 *  - [refresh]                : re-list from repository
 *  - [importLocal]            : add a scene by local path
 *  - [requestConversion]      : convert remote .ply → .ksplat (Phase 4)
 *  - [observeConversionState] : hot stream of conversion progress
 *
 * Output:
 *  - [state] : [SceneListState] consumed by the Compose screen
 *
 * The view model intentionally does not own the [SceneRepository] —
 * the Repository is injected so tests can use a fake without Room.
 */
class SceneListViewModel(
    private val repo: SceneRepository,
    private val api: AssetConversionApi,
) : ViewModel() {

    private val _state = MutableStateFlow(SceneListState())
    val state: StateFlow<SceneListState> = _state.asStateFlow()

    /** Threshold above which the asset is unlikely to live-render well. */
    private val oversizedSplatCount = 500_000L

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val scenes = repo.list()
            _state.value = _state.value.copy(scenes = scenes, isLoading = false)
        }
    }

    fun importLocal(asset: SceneAsset) {
        viewModelScope.launch {
            repo.upsert(asset)
            val warnings = mutableListOf<String>()
            if (asset.splatCount > oversizedSplatCount) {
                warnings += "Asset \"${asset.name}\" is oversized (${asset.splatCount} splats). Live wallpaper may fall back to STATIC."
            }
            // Re-list so the new asset is visible in the UI.
            val scenes = repo.list()
            _state.value = _state.value.copy(scenes = scenes, warnings = warnings)
        }
    }

    suspend fun requestConversion(request: ConversionRequest): ConversionState {
        val submitted: SubmittedConversion = api.submit(request)
        val pending = submitted.state.conversionId
        _state.value = _state.value.copy(
            pendingDownloads = _state.value.pendingDownloads + (pending to submitted.state),
        )
        // Drive the flow to completion; last state is returned to caller.
        var last: ConversionState = submitted.state
        api.poll(pending).collect { state ->
            last = state
            _state.value = _state.value.copy(
                pendingDownloads = _state.value.pendingDownloads + (pending to state),
            )
        }
        return last
    }
}

/** State consumed by [SceneListScreen]. */
data class SceneListState(
    val scenes: List<SceneAsset> = emptyList(),
    val isLoading: Boolean = false,
    val pendingDownloads: Map<String, ConversionState> = emptyMap(),
    val warnings: List<String> = emptyList(),
)