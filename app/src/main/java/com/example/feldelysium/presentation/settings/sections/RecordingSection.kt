package com.example.feldelysium.presentation.settings.sections

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.presentation.settings.SettingsEvent
import com.example.feldelysium.presentation.settings.lockedUnless

@Composable
internal fun RecordingSection(
    settings: AppSettings,
    onEvent: (SettingsEvent) -> Unit,
) {
    FeldSettingsSection(title = stringResource(R.string.settings_video_export_section)) {
        FeldSettingSwitchRow(
            icon = FeldIconId.Video,
            title = stringResource(R.string.settings_record_playback_title),
            subtitle = stringResource(R.string.settings_record_playback_subtitle),
            checked = settings.exportEnabled,
            onCheckedChange = { onEvent(SettingsEvent.SetExportEnabled(it)) },
        )
        FeldSettingsDivider()

        val audioSupported = Build.VERSION.SDK_INT >= 29

        FeldSettingSwitchRow(
            icon = FeldIconId.Microphone,
            title = stringResource(R.string.settings_record_sound_title),
            subtitle = stringResource(
                if (audioSupported) R.string.settings_record_sound_subtitle
                else R.string.settings_android_10_required,
            ),
            checked = settings.exportAudio && audioSupported,
            onCheckedChange = { onEvent(SettingsEvent.SetExportAudio(it)) },
            status = lockedUnless(settings.exportEnabled && audioSupported),
        )
    }
}
