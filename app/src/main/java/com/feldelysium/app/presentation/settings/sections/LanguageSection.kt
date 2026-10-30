package com.feldelysium.app.presentation.settings.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.app.presentation.settings.SettingsDialog
import com.feldelysium.app.presentation.shared.language.AppLanguage
import com.feldelysium.app.presentation.shared.language.displayName

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
