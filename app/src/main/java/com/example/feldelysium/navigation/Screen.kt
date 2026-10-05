package com.example.feldelysium.navigation

sealed class Screen(val route: String) {
    data object Editor   : Screen("editor")
    data object Playback : Screen("playback")
}