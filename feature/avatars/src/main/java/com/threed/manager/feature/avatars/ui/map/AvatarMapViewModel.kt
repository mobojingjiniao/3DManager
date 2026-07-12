package com.threed.manager.feature.avatars.ui.map

import androidx.lifecycle.ViewModel
import com.threed.manager.feature.avatars.AvatarRepositoryProvider
import com.threed.manager.feature.avatars.model.Avatar
import kotlinx.coroutines.flow.StateFlow

class AvatarMapViewModel : ViewModel() {
    val avatars: StateFlow<List<Avatar>> = AvatarRepositoryProvider.repository.avatars
}