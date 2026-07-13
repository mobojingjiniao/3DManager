package com.threed.manager.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraBottomBar
import com.threed.manager.core.design.components.AuroraFloatingAction
import com.threed.manager.core.design.theme.AuroraTheme
import com.threed.manager.feature.avatars.model.AvatarId
import com.threed.manager.feature.avatars.ui.capture.AvatarCaptureScreen
import com.threed.manager.feature.avatars.ui.detail.AvatarDetailScreen
import com.threed.manager.feature.avatars.ui.library.AvatarLibraryScreen
import com.threed.manager.feature.avatars.ui.map.AvatarMapScreen
import com.threed.manager.feature.avatars.ui.settings.SettingsScreen
import com.threed.manager.feature.avatars.ui.share.AvatarShareScreen
import com.threed.manager.feature.avatars.ui.wallpaper.WallpaperScreen

/** Routes for the top-level Compose Navigation graph. */
object AuroraRoutes {
    const val Avatars   = "avatars"
    const val Map       = "map"
    const val Wallpaper = "wallpaper"
    const val Settings  = "settings"
    const val Capture   = "capture/new"
    const val Detail    = "avatars/detail/{id}"
    const val Share     = "share/{id}"

    fun detail(id: String) = "avatars/detail/$id"
    fun share(id: String)  = "share/$id"
}

@Composable
fun AuroraRootNav(nav: NavHostController = rememberNavController()) {
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AuroraRoutes.Avatars

    val tabNavigate: (String) -> Unit = { route ->
        if (route != currentRoute) {
            nav.navigate(route) {
                popUpTo(AuroraRoutes.Avatars) { inclusive = false; saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val capture: () -> Unit = { nav.navigate(AuroraRoutes.Capture) }

    AuroraBackground {
        NavHost(
            navController = nav,
            startDestination = AuroraRoutes.Avatars,
        ) {
            composable(AuroraRoutes.Avatars) {
                AvatarLibraryScreen(
                    onOpenAvatar = { id -> nav.navigate(AuroraRoutes.detail(id.raw)) },
                    onOpenMap = { nav.navigate(AuroraRoutes.Map) },
                    onCapture = capture,
                    onTabNavigate = tabNavigate,
                )
            }
            composable(AuroraRoutes.Map) {
                AvatarMapScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAvatar = { id -> nav.navigate(AuroraRoutes.detail(id.raw)) },
                    onTabNavigate = tabNavigate,
                )
            }
            composable(AuroraRoutes.Wallpaper) { WallpaperScreen(onTabNavigate = tabNavigate) }
            composable(AuroraRoutes.Settings) { SettingsScreen(onTabNavigate = tabNavigate) }

            composable(AuroraRoutes.Capture) {
                AvatarCaptureScreen(
                    onBack = { nav.popBackStack() },
                    onClose = { nav.popBackStack() },
                )
            }
            composable(
                AuroraRoutes.Detail,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val raw = entry.arguments?.getString("id") ?: ""
                AvatarDetailScreen(
                    avatarId = AvatarId(raw),
                    onBack = { nav.popBackStack() },
                    onShare = { nav.navigate(AuroraRoutes.share(raw)) },
                    onEdit = { /* TODO Phase 6 */ },
                    onOpenMap = { nav.navigate(AuroraRoutes.Map) },
                    onSetWallpaper = { /* handled inside DetailContent */ },
                )
            }
            composable(
                AuroraRoutes.Share,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val raw = entry.arguments?.getString("id") ?: ""
                AvatarShareScreen(avatarId = raw, onBack = { nav.popBackStack() })
            }
        }
    }
}

/** Stub composable for routes not yet implemented. */
@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, color = AuroraTheme.colors.TextPrimary, style = AuroraTheme.type.displaySection)
    }
}