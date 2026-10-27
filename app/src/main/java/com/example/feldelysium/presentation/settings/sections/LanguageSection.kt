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

@Composable
internal fun LanguageSection(onOpenDialog: (SettingsDialog) -> Unit) {
    val haptics = rememberFeldHaptics()

    FeldSettingsSection(title = stringResource(R.string.settings_language_title)) {
        FeldSettingValueRow(
            icon = FeldIconId.Language,
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_subtitle),
            value = when (AppLanguage.current()) {
                AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
                AppLanguage.ENGLISH -> "English"
                AppLanguage.BELARUSIAN -> "Беларуская"
            },
            onClick = { haptics.tick(); onOpenDialog(SettingsDialog.Language) },
        )
    }
}
