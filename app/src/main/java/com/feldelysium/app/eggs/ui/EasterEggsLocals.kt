package com.feldelysium.app.eggs.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.feldelysium.app.eggs.EasterEggs

val LocalEasterEggs = staticCompositionLocalOf<EasterEggs?> { null }

@Composable
fun SuppressEasterEggs() {
    val eggs = LocalEasterEggs.current
    DisposableEffect(eggs) {
        eggs?.setSuppressed(true)
        onDispose { eggs?.setSuppressed(false) }
    }
}
