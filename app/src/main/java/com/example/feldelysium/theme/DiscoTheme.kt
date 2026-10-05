package com.example.feldelysium.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DiscoColorScheme = darkColorScheme(
    primary = DiscoPrimary,
    secondary = DiscoSecondary,
    background = DiscoBackground,
    surface = DiscoSurface,
    onSurface = DiscoOnSurface,
    error = DiscoError,
    onError = DiscoOnError
)

@Composable
fun DiscoDialogueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiscoColorScheme,
        typography = DiscoTypography,
        content = content
    )
}