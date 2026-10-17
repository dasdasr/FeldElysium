package com.example.feldelysium.navigation

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
        startDestination = Screen.Settings.route,
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
