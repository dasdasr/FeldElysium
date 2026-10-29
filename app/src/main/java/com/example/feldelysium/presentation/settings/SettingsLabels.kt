package com.example.feldelysium.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.settings.FeldSettingStatus
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.domain.model.settings.BackgroundKind
import com.example.feldelysium.domain.model.settings.SamplesMode
import com.example.feldelysium.domain.model.settings.TapMode

internal fun lockedUnless(enabled: Boolean): FeldSettingStatus? = if (enabled) null else FeldSettingStatus.LOCK

@Composable
internal fun TapMode.label(): String = stringResource(
    when (this) {
        TapMode.WHOLE_SCREEN -> R.string.settings_tap_mode_whole_screen
        TapMode.CONTINUE_BUTTON -> R.string.settings_tap_mode_continue_button
        TapMode.NOWHERE -> R.string.settings_tap_mode_nowhere
    },
)

@Composable
internal fun TapMode.subtitle(): String = stringResource(
    when (this) {
        TapMode.WHOLE_SCREEN -> R.string.settings_advance_whole_screen
        TapMode.CONTINUE_BUTTON -> R.string.settings_advance_continue_button
        TapMode.NOWHERE -> R.string.settings_advance_autoplay_only
    },
)

@Composable
internal fun SamplesMode.label(): String = stringResource(
    when (this) {
        SamplesMode.AUTO -> R.string.settings_samples_auto
        SamplesMode.ALWAYS_SHOW -> R.string.settings_samples_show
        SamplesMode.ALWAYS_HIDE -> R.string.settings_samples_hide
    },
)

@Composable
internal fun AppSettings.samplesSubtitle(): String = stringResource(
    when (samplesMode) {
        SamplesMode.AUTO ->
            if (packImported) {
                R.string.settings_samples_imported_hidden
            } else {
                R.string.settings_samples_hide_after_import
            }

        SamplesMode.ALWAYS_SHOW -> R.string.settings_samples_always_visible

        SamplesMode.ALWAYS_HIDE -> R.string.settings_samples_always_hidden
    },
)

@Composable
internal fun AppSettings.backgroundLabel(): String = stringResource(
    when {
        backgroundKind == BackgroundKind.COLOR -> R.string.settings_background_color
        backgroundMediaUri.isBlank() -> R.string.settings_background_default
        else -> R.string.settings_background_custom
    },
)
