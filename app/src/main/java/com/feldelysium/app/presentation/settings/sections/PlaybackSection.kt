package com.feldelysium.app.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.presentation.settings.SettingsEvent
import com.feldelysium.app.presentation.settings.label
import com.feldelysium.app.presentation.settings.lockedUnless
import com.feldelysium.app.presentation.settings.subtitle
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics

@Composable
internal fun PlaybackSection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit) {
    val haptics = rememberFeldHaptics()

    FeldSettingsSection(title = stringResource(R.string.settings_playback_section)) {
        FeldSettingSwitchRow(
            icon = FeldIconId.Autoplay,
            title = stringResource(R.string.settings_autoplay_title),
            subtitle = stringResource(R.string.settings_autoplay_subtitle),
            checked = settings.autoPlay,
            onCheckedChange = { onEvent(SettingsEvent.SetAutoPlay(it)) },
        )

        if (settings.autoPlay) {
            FeldSettingsDivider()
            FeldSettingValueRow(
                icon = FeldIconId.Speed,
                title = stringResource(R.string.settings_autoplay_speed_title),
                subtitle = stringResource(R.string.settings_autoplay_speed_subtitle),
                value = settings.autoPlaySpeed.label,
                onClick = {
                    haptics.tick()
                    onEvent(SettingsEvent.CycleAutoPlaySpeed)
                },
            )
        }
        FeldSettingsDivider()

        FeldSettingSwitchRow(
            icon = FeldIconId.PlaybackText,
            title = stringResource(R.string.settings_continue_button_title),
            subtitle = stringResource(R.string.settings_continue_button_subtitle),
            checked = settings.showContinueButton,
            onCheckedChange = { onEvent(SettingsEvent.ShowContinueButton(it)) },
        )
        FeldSettingsDivider()

        FeldSettingValueRow(
            icon = FeldIconId.Play,
            title = stringResource(R.string.settings_advance_dialogue_title),
            subtitle = settings.tapMode.subtitle(),
            status = lockedUnless(settings.showContinueButton),
            value = settings.tapMode.label(),
            onClick = {
                haptics.tick()
                onEvent(SettingsEvent.CycleTapMode)
            },
        )
    }
}
