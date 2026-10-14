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
    tertiaryContainer = FeldSurfaceHigh,
    onTertiaryContainer = DiscoText,

    background = FeldBackground,
    onBackground = DiscoText,
    surface = FeldSurface,
    onSurface = DiscoText,
    surfaceVariant = FeldSurfaceHigh,
    onSurfaceVariant = DiscoTextMuted,
    surfaceTint = Color.Transparent,
    inverseSurface = DiscoCream,
    inverseOnSurface = DiscoInk,

    error = DiscoError,
    onError = DiscoText,
    errorContainer = DiscoError,
    onErrorContainer = DiscoText,

    outline = FeldOutline,
    outlineVariant = FeldOutline,

    surfaceDim = FeldBackground,
    surfaceBright = FeldSurfaceHigh,
    surfaceContainerLowest = FeldBackground,
    surfaceContainerLow = FeldSurface,
    surfaceContainer = FeldSurface,
    surfaceContainerHigh = FeldSurfaceHigh,
    surfaceContainerHighest = FeldSurfaceHigh,
)

@Immutable
data class DiscoExtraColors(
    val success: Color = DiscoSuccess,
    val primaryPressed: Color = DiscoOrangePressed,
    val accent: Color = DiscoTeal,
)

@Composable
fun discoSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = DiscoText,
    checkedTrackColor = DiscoOrange,
    checkedBorderColor = DiscoOrange,
    uncheckedThumbColor = DiscoTextMuted,
    uncheckedTrackColor = FeldOutline,
    uncheckedBorderColor = FeldOutline,
)

private val LocalDiscoColors = staticCompositionLocalOf { DiscoExtraColors() }

val MaterialTheme.disco: DiscoExtraColors
    @Composable @ReadOnlyComposable
    get() = LocalDiscoColors.current

@Composable
fun DiscoDialogueTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDiscoColors provides DiscoExtraColors()) {
        MaterialTheme(
            colorScheme = DiscoColorScheme,
            typography = FeldTypography,
            content = content,
        )
    }
}
