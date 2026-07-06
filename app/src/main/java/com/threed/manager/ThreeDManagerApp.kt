package com.threed.manager

import android.app.Application

/**
 * Root Application for 3DManager.
 *
 * Phase 0: simple Application without DI.
 * Phase 1+: will be migrated to @HiltAndroidApp with a proper Hilt setup
 * (the Hilt+KSP wiring needs network artifacts that aren't cached here).
 */
class ThreeDManagerApp : Application()
