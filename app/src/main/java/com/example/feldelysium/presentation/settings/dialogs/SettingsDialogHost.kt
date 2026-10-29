package com.example.feldelysium.presentation.settings.dialogs

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.settings.SettingsDialog
import com.example.feldelysium.presentation.settings.SettingsEvent
import com.example.feldelysium.presentation.settings.speakerpack.SpeakerPackViewModel
import com.example.feldelysium.presentation.shared.dialogs.ManageTagsDialog
import com.example.feldelysium.presentation.shared.language.AppLanguage

@Composable
internal fun SettingsDialogHost(
    dialog: SettingsDialog?,
    settings: AppSettings,
    packVm: SpeakerPackViewModel,
    onEvent: (SettingsEvent) -> Unit,
    onImportFont: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val eggs = LocalEasterEggs.current
    val haptics = rememberFeldHaptics()

    when (dialog) {
        null -> Unit

        SettingsDialog.PlaybackUiText -> PlaybackUiTextDialog(
            money = settings.playbackMoney,
            time = settings.playbackTime,
            day = settings.playbackDay,
            onMoneyChange = { onEvent(SettingsEvent.SetPlaybackMoney(it)) },
            onTimeChange = { onEvent(SettingsEvent.SetPlaybackTime(it)) },
            onDayChange = { onEvent(SettingsEvent.SetPlaybackDay(it)) },
            onReset = {
                haptics.thud()
                if (settings.playbackTextIsDefault()) eggs?.report(EggEvent.ResetUnchanged)
                onEvent(SettingsEvent.ResetPlaybackUiText)
            },
            onDismiss = onDismiss,
        )

        SettingsDialog.Background -> BackgroundPickerDialog(
            settings = settings,
            onKind = { onEvent(SettingsEvent.SetBackgroundKind(it)) },
            onColor = { onEvent(SettingsEvent.SetBackgroundColor(it)) },
            onMedia = { uri, isVideo -> onEvent(SettingsEvent.SetBackgroundMedia(uri, isVideo)) },
            onVideoSound = { onEvent(SettingsEvent.SetBackgroundVideoSound(it)) },
            onVideoFill = { onEvent(SettingsEvent.SetBackgroundVideoFill(it)) },
            onReset = {
                haptics.thud()
                if (settings.backgroundIsDefault()) eggs?.report(EggEvent.ResetUnchanged)
                onEvent(SettingsEvent.ResetBackground)
            },
            onDismiss = onDismiss,
        )

        SettingsDialog.BarIcons -> BarIconsDialog(
            icons = settings.barIcons,
            inventory = settings.inventoryIcons,
            onCycleIcon = { slot ->
                haptics.tick()
                onEvent(SettingsEvent.CycleBarIcon(slot))
            },
            onCycleInventory = { slot ->
                haptics.tick()
                onEvent(SettingsEvent.CycleInventoryIcon(slot))
            },
            onReset = {
                haptics.thud()
                onEvent(SettingsEvent.ResetBarIcons)
            },
            onDismiss = onDismiss,
        )

        SettingsDialog.Font -> PlaybackFontDialog(
            settings = settings,
            onSelect = { onEvent(SettingsEvent.SetFontChoice(it)) },
            onReset = { onEvent(SettingsEvent.SetFontChoice("")) },
            onUpload = onImportFont,
            onDismiss = onDismiss,
        )

        SettingsDialog.Language -> LanguageDialog(
            current = AppLanguage.current(),
            onSelect = { AppLanguage.apply(it) },
            onDismiss = onDismiss,
        )

        SettingsDialog.Tags -> TagsDialogContainer(packVm, onDismiss)
    }

    ImportConflictContainer(packVm)
}

@Composable
private fun TagsDialogContainer(packVm: SpeakerPackViewModel, onDismiss: () -> Unit) {
    val tags by packVm.tags.collectAsStateWithLifecycle()
    val speakers by packVm.speakers.collectAsStateWithLifecycle()

    ManageTagsDialog(
        tags = tags,
        speakers = speakers,
        onSave = packVm::saveTag,
        onDelete = packVm::deleteTag,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ImportConflictContainer(packVm: SpeakerPackViewModel) {
    val pending by packVm.pending.collectAsStateWithLifecycle()
    pending?.let {
        ImportConflictDialog(
            preview = it.preview,
            onConfirm = packVm::confirmImport,
            onDismiss = packVm::cancelImport,
        )
    }
}

private fun AppSettings.playbackTextIsDefault(): Boolean {
    val d = AppSettings()
    return playbackMoney == d.playbackMoney && playbackTime == d.playbackTime && playbackDay == d.playbackDay
}

private fun AppSettings.backgroundIsDefault(): Boolean {
    val d = AppSettings()
    return backgroundKind == d.backgroundKind &&
        backgroundColorArgb == d.backgroundColorArgb &&
        backgroundMediaUri == d.backgroundMediaUri &&
        backgroundMediaIsVideo == d.backgroundMediaIsVideo &&
        backgroundVideoSound == d.backgroundVideoSound &&
        backgroundVideoFill == d.backgroundVideoFill
}
