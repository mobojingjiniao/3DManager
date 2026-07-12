package com.threed.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.threed.manager.core.design.theme.AuroraTheme
import com.threed.manager.navigation.AuroraRootNav

/**
 * Main entry — Cinematic Dark Aurora v3.2.
 *
 * Hosts the top-level [AuroraRootNav] Compose graph. The legacy native
 * [SplatBackgroundView] can still be invoked by [ThreeDManagerApp] via
 * the service-locator; this Activity uses Compose-only chrome.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: transparent system bars over our Void/Base background.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AuroraTheme {
                AuroraRootNav()
            }
        }
    }
}