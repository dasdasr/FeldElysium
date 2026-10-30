package com.feldelysium.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Editor : AppRoute

    @Serializable
    data object Playback : AppRoute

    @Serializable
    data object Settings : AppRoute
}
