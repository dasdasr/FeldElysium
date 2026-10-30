package com.feldelysium.app.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingRow
import com.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.presentation.settings.SettingsDialog
import com.feldelysium.app.presentation.settings.SettingsEvent
import com.feldelysium.app.presentation.settings.label
import com.feldelysium.app.presentation.settings.samplesSubtitle

@Composable
internal fun SpeakersSection(
    settings: AppSettings,
    onEvent: (SettingsEvent) -> Unit,
    onOpenDialog: (SettingsDialog) -> Unit,
    onExportPack: () -> Unit,
    onImportPack: () -> Unit,
) {
    val haptics = rememberFeldHaptics()

    FeldSettingsSection(title = stringResource(R.string.settings_speakers_section)) {
        FeldSettingRow(
            icon = FeldIconId.Export,
            title = stringResource(R.string.settings_export_speakers_title),
            subtitle = stringResource(R.string.settings_export_speakers_subtitle),
            showChevron = true,
            onClick = {
                haptics.tick()
                onExportPack()
            },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Import,
            title = stringResource(R.string.settings_import_speakers_title),
            subtitle = stringResource(R.string.settings_import_speakers_subtitle),
            showChevron = true,
            onClick = {
                haptics.tick()
                onImportPack()
            },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Tag,
            title = stringResource(R.string.settings_tags_title),
            subtitle = stringResource(R.string.settings_tags_subtitle),
            showChevron = true,
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.Tags)
            },
        )
        FeldSettingsDivider()

        FeldSettingValueRow(
            icon = FeldIconId.AuthorSamples,
            title = stringResource(R.string.settings_author_samples_title),
            subtitle = settings.samplesSubtitle(),
            value = settings.samplesMode.label(),
            onClick = {
                haptics.tick()
                onEvent(SettingsEvent.CycleSamplesMode)
            },
        )
    }
}
