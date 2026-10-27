package com.example.feldelysium.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.settings.SettingsEvent
import com.example.feldelysium.presentation.settings.backgroundLabel
import com.example.feldelysium.presentation.settings.components.FeldBackgroundRow
import com.example.feldelysium.designsystem.theme.FONT_CUSTOM
import com.example.feldelysium.designsystem.theme.prettyFontName
import com.example.feldelysium.presentation.settings.SettingsDialog
import java.io.File

@Composable
internal fun AppearanceSection(
    settings: AppSettings,
    freezeVideo: Boolean,
    onEvent: (SettingsEvent) -> Unit,
    onOpenDialog: (SettingsDialog) -> Unit,
) {
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current

    FeldSettingsSection(title = stringResource(R.string.settings_appearance_section)) {
        FeldBackgroundRow(
            settings = settings,
            currentLabel = settings.backgroundLabel(),
            freezeVideo = freezeVideo,
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.Background) },
        )
        FeldSettingsDivider()

        FeldSettingValueRow(
            icon = FeldIconId.Typeface,
            title = stringResource(R.string.settings_typeface_title),
            subtitle = stringResource(R.string.settings_typeface_subtitle),
            value = when (settings.fontChoice) {
                "" -> "Alegreya"
                FONT_CUSTOM -> settings.customFontName
                else -> prettyFontName(File(settings.fontChoice))
            },
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.Font) },
        )
        FeldSettingsDivider()

        val cinemaFrameTitle = stringResource(R.string.settings_cinema_frame_title)
        FeldSettingSwitchRow(
            icon = FeldIconId.Cinema,
            title = cinemaFrameTitle,
            subtitle = stringResource(R.string.settings_cinema_frame_subtitle),
            checked = settings.lockAspectRatio16x9,
            onCheckedChange = {
                eggs?.report(EggEvent.SwitchToggle, cinemaFrameTitle)
                onEvent(SettingsEvent.SetLockAspectRatio16x9(it))
            },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.PanelCalendar,
            title = stringResource(R.string.settings_status_line_title),
            subtitle = stringResource(R.string.settings_status_line_subtitle),
            showChevron = true,
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.PlaybackUiText) },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Panel,
            title = stringResource(R.string.settings_panel_icons_title),
            subtitle = stringResource(R.string.settings_panel_icons_subtitle),
            showChevron = true,
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.BarIcons) },
        )
    }
}
