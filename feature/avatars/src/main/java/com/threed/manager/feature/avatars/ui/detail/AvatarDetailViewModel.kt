package com.threed.manager.feature.avatars.ui.detail

import androidx.lifecycle.ViewModel
import com.threed.manager.feature.avatars.AvatarRepositoryProvider
import com.threed.manager.feature.avatars.model.Avatar
import com.threed.manager.feature.avatars.model.AvatarId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AvatarDetailViewModel : ViewModel() {
    private val repo = AvatarRepositoryProvider.repository

    fun get(id: AvatarId): Flow<Avatar?> =
        repo.avatars.map { list -> list.firstOrNull { it.id == id } }
}