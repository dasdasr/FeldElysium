package com.feldelysium.app.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.eggs.EggEvent
import com.feldelysium.app.eggs.ui.LocalEasterEggs
import com.feldelysium.app.presentation.settings.SettingsDialog
import com.feldelysium.app.presentation.settings.SettingsEvent
import com.feldelysium.app.presentation.settings.backgroundLabel
import com.feldelysium.app.presentation.settings.components.FeldBackgroundRow
import com.feldelysium.app.presentation.theme.FONT_CUSTOM
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingRow
import com.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.theme.prettyFontName
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
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.Background)
            },
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
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.Font)
            },
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
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.PlaybackUiText)
            },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Panel,
            title = stringResource(R.string.settings_panel_icons_title),
            subtitle = stringResource(R.string.settings_panel_icons_subtitle),
            showChevron = true,
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.BarIcons)
            },
        )
    }
}
