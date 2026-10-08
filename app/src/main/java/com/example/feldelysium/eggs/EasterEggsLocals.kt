package com.example.feldelysium.eggs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

val LocalEasterEggs = staticCompositionLocalOf<EasterEggs?> { null }

@Composable
fun SuppressEasterEggs() {
    val eggs = LocalEasterEggs.current
    DisposableEffect(eggs) {
        eggs?.setSuppressed(true)
        onDispose { eggs?.setSuppressed(false) }
    }
}
