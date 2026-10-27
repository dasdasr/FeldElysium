package com.example.feldelysium.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.presentation.settings.SettingsDialog
import com.example.feldelysium.presentation.settings.SettingsEvent
import com.example.feldelysium.presentation.settings.label
import com.example.feldelysium.presentation.settings.samplesSubtitle

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
            onClick = { haptics.tick(); onExportPack() },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Import,
            title = stringResource(R.string.settings_import_speakers_title),
            subtitle = stringResource(R.string.settings_import_speakers_subtitle),
            showChevron = true,
            onClick = { haptics.tick(); onImportPack() },
        )
        FeldSettingsDivider()

        FeldSettingRow(
            icon = FeldIconId.Tag,
            title = stringResource(R.string.settings_tags_title),
            subtitle = stringResource(R.string.settings_tags_subtitle),
            showChevron = true,
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.Tags) },
        )
        FeldSettingsDivider()

        FeldSettingValueRow(
            icon = FeldIconId.AuthorSamples,
            title = stringResource(R.string.settings_author_samples_title),
            subtitle = settings.samplesSubtitle(),
            value = settings.samplesMode.label(),
            onClick = { haptics.tick(); onEvent(SettingsEvent.CycleSamplesMode) },
        )
    }
}
