package com.feldelysium.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry

fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

/** Runs the action only while this screen is fully on top, not mid-transition. */
@Composable
fun rememberResumedGuard(): (() -> Unit) -> Unit {
    val owner = LocalLifecycleOwner.current
    return remember(owner) {
        { action -> if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action() }
    }
}
