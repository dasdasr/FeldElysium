package com.feldelysium.app.presentation.settings.sections

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.presentation.settings.SettingsEvent
import com.feldelysium.app.presentation.settings.lockedUnless

@Composable
internal fun RecordingSection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit) {
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
                if (audioSupported) {
                    R.string.settings_record_sound_subtitle
                } else {
                    R.string.settings_android_10_required
                },
            ),
            checked = settings.exportAudio && audioSupported,
            onCheckedChange = { onEvent(SettingsEvent.SetExportAudio(it)) },
            status = lockedUnless(settings.exportEnabled && audioSupported),
        )
    }
}
