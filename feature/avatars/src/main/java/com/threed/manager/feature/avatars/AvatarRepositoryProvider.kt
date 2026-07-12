package com.threed.manager.feature.avatars

import com.threed.manager.feature.avatars.model.AvatarRepository

/**
 * Single shared [AvatarRepository] instance for the app. Held statically
 * so service-locator style consumers (Compose ViewModels, Application)
 * can pick it up without DI overhead. Replace with Hilt in Phase 2.
 */
object AvatarRepositoryProvider {
    val repository: AvatarRepository = AvatarRepository()
}