package com.feldelysium.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val FeldColorScheme = darkColorScheme(
    primary = FeldOrange,
    onPrimary = FeldInk,
    primaryContainer = FeldOrange,
    onPrimaryContainer = FeldInk,
    inversePrimary = FeldOrangePressed,

    secondary = FeldCream,
    onSecondary = FeldInk,
    secondaryContainer = FeldCream,
    onSecondaryContainer = FeldInk,

    tertiary = FeldOrangePressed,
    onTertiary = FeldText,
    tertiaryContainer = FeldSurfaceHigh,
    onTertiaryContainer = FeldText,

    background = FeldBackground,
    onBackground = FeldText,
    surface = FeldSurface,
    onSurface = FeldText,
    surfaceVariant = FeldSurfaceHigh,
    onSurfaceVariant = FeldTextMuted,
    surfaceTint = Color.Transparent,
    inverseSurface = FeldCream,
    inverseOnSurface = FeldInk,

    error = FeldError,
    onError = FeldText,
    errorContainer = FeldError,
    onErrorContainer = FeldText,

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
public data class FeldExtraColors(
    val success: Color = FeldSuccess,
    val primaryPressed: Color = FeldOrangePressed,
    val accent: Color = FeldTeal,
    val checkFailure: Color = Color(0xFFB3141A),
    val damageMorale: Color = Color(0xFF7A3FB0),
    val damageHealth: Color = Color(0xFFC0392B),
)

@Composable
internal fun feldSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = FeldText,
    checkedTrackColor = FeldOrange,
    checkedBorderColor = FeldOrange,
    uncheckedThumbColor = FeldTextMuted,
    uncheckedTrackColor = FeldOutline,
    uncheckedBorderColor = FeldOutline,
)

private val LocalFeldColors = staticCompositionLocalOf { FeldExtraColors() }

public val MaterialTheme.feld: FeldExtraColors
    @Composable @ReadOnlyComposable
    get() = LocalFeldColors.current

@Composable
public fun FeldTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFeldColors provides FeldExtraColors()) {
        MaterialTheme(
            colorScheme = FeldColorScheme,
            typography = FeldTypography,
            content = content,
        )
    }
}
