package com.example.feldelysium.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.presentation.settings.SettingsDialog
import com.example.feldelysium.presentation.shared.language.AppLanguage
import com.example.feldelysium.presentation.shared.language.displayName

@Composable
internal fun LanguageSection(onOpenDialog: (SettingsDialog) -> Unit) {
    val haptics = rememberFeldHaptics()

    FeldSettingsSection(title = stringResource(R.string.settings_language_title)) {
        FeldSettingValueRow(
            icon = FeldIconId.Language,
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_subtitle),
            value = AppLanguage.current().displayName(),
            onClick = {
                haptics.tick()
                onOpenDialog(SettingsDialog.Language)
            },
        )
    }
}
