package com.threed.manager.core.system

/** Coarse device classification. Determined at startup from Build / GPU. */
enum class DeviceTier { Flagship, Mid, Low }

/**
 * Lightweight device profile. Pure-data; no Android dependencies so the
 * strategy is testable on the JVM.
 */
data class DeviceProfile(
    val tier: DeviceTier,
    val isOemBlocklisted: Boolean = false,
    val webgl2Available: Boolean = true,
)
