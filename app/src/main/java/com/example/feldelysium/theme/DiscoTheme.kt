package com.example.feldelysium.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DiscoColorScheme = darkColorScheme(
    primary = DiscoOrange,
    onPrimary = DiscoInk,
    primaryContainer = DiscoOrange,
    onPrimaryContainer = DiscoInk,
    inversePrimary = DiscoOrangePressed,

    secondary = DiscoCream,
    onSecondary = DiscoInk,
    secondaryContainer = DiscoCream,
    onSecondaryContainer = DiscoInk,

    tertiary = DiscoOrangePressed,
    onTertiary = DiscoText,
    tertiaryContainer = DiscoSurfaceHigh,
    onTertiaryContainer = DiscoText,

    background = DiscoBackground,
    onBackground = DiscoText,
    surface = DiscoSurface,
    onSurface = DiscoText,
    surfaceVariant = DiscoSurfaceHigh,
    onSurfaceVariant = DiscoTextMuted,
    surfaceTint = Color.Transparent,
    inverseSurface = DiscoCream,
    inverseOnSurface = DiscoInk,

    error = DiscoError,
    onError = DiscoText,
    errorContainer = DiscoError,
    onErrorContainer = DiscoText,

    outline = DiscoOutline,
    outlineVariant = DiscoOutline,

    surfaceDim = DiscoBackground,
    surfaceBright = DiscoSurfaceHigh,
    surfaceContainerLowest = DiscoBackground,
    surfaceContainerLow = DiscoSurface,
    surfaceContainer = DiscoSurface,
    surfaceContainerHigh = DiscoSurfaceHigh,
    surfaceContainerHighest = DiscoSurfaceHigh,
)

@Immutable
data class DiscoExtraColors(
    val success: Color = DiscoSuccess,
    val primaryPressed: Color = DiscoOrangePressed,
)

@Composable
fun discoSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = DiscoText,
    checkedTrackColor = DiscoOrange,
    checkedBorderColor = DiscoOrange,
    uncheckedThumbColor = DiscoTextMuted,
    uncheckedTrackColor = DiscoOutline,
    uncheckedBorderColor = DiscoOutline,
)

private val LocalDiscoColors = staticCompositionLocalOf { DiscoExtraColors() }

val MaterialTheme.disco: DiscoExtraColors
    @Composable @ReadOnlyComposable get() = LocalDiscoColors.current

@Composable
fun DiscoDialogueTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDiscoColors provides DiscoExtraColors()) {
        MaterialTheme(
            colorScheme = DiscoColorScheme,
            typography = DiscoTypography,
            content = content
        )
    }
}
