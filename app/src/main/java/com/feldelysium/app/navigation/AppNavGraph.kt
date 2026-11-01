package com.feldelysium.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.feldelysium.app.presentation.editor.EditorScreen
import com.feldelysium.app.presentation.playback.PlaybackScreen
import com.feldelysium.app.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Editor,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            scaleOut(
                targetScale = 0.94f,
                animationSpec = tween(durationMillis = 160),
            ) + fadeOut(
                animationSpec = tween(durationMillis = 160),
            )
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            scaleOut(
                targetScale = 0.94f,
                animationSpec = tween(durationMillis = 160),
            ) + fadeOut(
                animationSpec = tween(durationMillis = 160),
            )
        },
    ) {
        composable<AppRoute.Editor> {
            EditorScreen(
                onNavigateToPlayback = {
                    navController.navigate(AppRoute.Playback)
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)
                },
            )
        }

        composable<AppRoute.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.Playback> {
            PlaybackScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
