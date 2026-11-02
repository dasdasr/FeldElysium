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
        composable<AppRoute.Editor> { entry ->
            EditorScreen(
                onNavigateToPlayback = { if (entry.isResumed()) navController.navigate(AppRoute.Playback) },
                onNavigateToSettings = { if (entry.isResumed()) navController.navigate(AppRoute.Settings) },
            )
        }
        composable<AppRoute.Settings> { entry ->
            SettingsScreen(onNavigateBack = { if (entry.isResumed()) navController.popBackStack() })
        }
        composable<AppRoute.Playback> { entry ->
            PlaybackScreen(onNavigateBack = { if (entry.isResumed()) navController.popBackStack() })
        }
    }
}
