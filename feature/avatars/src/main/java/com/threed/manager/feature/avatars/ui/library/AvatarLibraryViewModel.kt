package com.threed.manager.feature.avatars.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threed.manager.feature.avatars.AvatarRepositoryProvider
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.model.AvatarSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AvatarFilter { ALL, CAPTURED, IMPORTED, FAVORITES }

data class AvatarLibraryUiState(
    val avatars: List<Avatar> = emptyList(),
    val filter: AvatarFilter = AvatarFilter.ALL,
    val isLoading: Boolean = false,
) {
    val visible: List<Avatar>
        get() = when (filter) {
            AvatarFilter.ALL       -> avatars
            AvatarFilter.CAPTURED  -> avatars.filter { it.source == AvatarSource.CAPTURED }
            AvatarFilter.IMPORTED  -> avatars.filter { it.source == AvatarSource.IMPORTED }
            AvatarFilter.FAVORITES -> avatars.filter { it.favorite }
        }
}

class AvatarLibraryViewModel : ViewModel() {
    private val repo = AvatarRepositoryProvider.repository
    private val filter = MutableStateFlow(AvatarFilter.ALL)

    val state: StateFlow<AvatarLibraryUiState> =
        combine(repo.avatars, filter) { avatars, f ->
            AvatarLibraryUiState(avatars = avatars, filter = f)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AvatarLibraryUiState(isLoading = true))

    fun setFilter(value: AvatarFilter) { filter.value = value }
    fun toggleFavorite(id: AvatarId) {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }
}