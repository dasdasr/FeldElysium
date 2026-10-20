package com.example.feldelysium.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feldelysium.presentation.editor.EditorScreen
import com.example.feldelysium.presentation.playback.PlaybackScreen
import com.example.feldelysium.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Editor.route,
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
        composable(Screen.Editor.route) {
            EditorScreen(
                onNavigateToPlayback = {
                    navController.navigate(Screen.Playback.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Playback.route) {
            PlaybackScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
