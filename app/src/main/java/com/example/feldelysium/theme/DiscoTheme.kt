package com.example.feldelysium.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DiscoColorScheme = darkColorScheme(
    primary = DiscoPrimary,
    onPrimary = DiscoOnPrimary,
    primaryContainer = DiscoPrimary,
    onPrimaryContainer = DiscoOnPrimary,
    inversePrimary = DiscoPrimaryPressed,

    secondary = DiscoSecondary,
    onSecondary = DiscoOnSurface,
    secondaryContainer = DiscoSecondary,
    onSecondaryContainer = DiscoOnSurface,

    tertiary = DiscoPlateRare,
    onTertiary = DiscoOnPlateRare,
    tertiaryContainer = DiscoPlateDone,
    onTertiaryContainer = DiscoOnPlateDone,

    background = DiscoBackground,
    onBackground = DiscoOnSurface,
    surface = DiscoSurface,
    onSurface = DiscoOnSurface,
    surfaceVariant = DiscoSurfaceVariant,
    onSurfaceVariant = DiscoTextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = DiscoPlateLight,
    inverseOnSurface = DiscoOnPlateLight,

    error = DiscoError,
    onError = DiscoOnError,
    errorContainer = DiscoButtonConfirm,
    onErrorContainer = DiscoOnError,

    outline = DiscoOutline,
    outlineVariant = DiscoOutline,

    surfaceDim = DiscoBackground,
    surfaceBright = DiscoSurfaceHigh,
    surfaceContainerLowest = DiscoBackground,
    surfaceContainerLow = DiscoSurface,
    surfaceContainer = DiscoSurfaceVariant,
    surfaceContainerHigh = DiscoSurfaceHigh,
    surfaceContainerHighest = DiscoSurfaceHigh,
)

@Immutable
data class DiscoExtraColors(
    val textTertiary: Color = DiscoTextTertiary,
    val primaryPressed: Color = DiscoPrimaryPressed,
    val primarySoft: Color = DiscoPrimarySoft,
    val inactiveOption: Color = DiscoInactiveOption,
    val inactiveText: Color = DiscoInactiveText,
    val buttonConfirm: Color = DiscoButtonConfirm,
    val plateDone: Color = DiscoPlateDone,
    val onPlateDone: Color = DiscoOnPlateDone,
    val plateRare: Color = DiscoPlateRare,
    val onPlateRare: Color = DiscoOnPlateRare,
    val plateLight: Color = DiscoPlateLight,
    val onPlateLight: Color = DiscoOnPlateLight,
    val success: Color = DiscoSuccess,
)

private val LocalDiscoColors = staticCompositionLocalOf { DiscoExtraColors() }

val MaterialTheme.disco: DiscoExtraColors
    @Composable @ReadOnlyComposable get() = LocalDiscoColors.current

@Composable
fun discoSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = DiscoOnSurface,
    checkedTrackColor = DiscoPrimary,
    checkedBorderColor = DiscoPrimary,
    uncheckedThumbColor = DiscoTextTertiary,
    uncheckedTrackColor = DiscoOutline,
    uncheckedBorderColor = DiscoOutline,
)

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
