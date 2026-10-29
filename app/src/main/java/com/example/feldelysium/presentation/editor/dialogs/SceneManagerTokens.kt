package com.example.feldelysium.presentation.editor.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.example.feldelysium.designsystem.foundation.material.accentBrush

internal val AccentOrange = Color(0xFFE45720)
internal val Ink = Color(0xFF1B1612)

@Composable
internal fun rememberOrangeBrush(seed: Int): Brush {
    val density = LocalDensity.current.density
    return remember(seed, density) {
        accentBrush(AccentOrange, seed, density, fade = 0.5f, glow = 0.3f, grain = 0.04f)
    }
}
