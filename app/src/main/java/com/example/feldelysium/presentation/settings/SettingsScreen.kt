package com.example.feldelysium.presentation.settings

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.FeldApp
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.data.settings.SamplesMode
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.designsystem.components.toast.FeldToastHost
import com.example.feldelysium.designsystem.components.toast.rememberFeldToastState
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.editor.components.ImportConflictDialog
import com.example.feldelysium.presentation.editor.components.ManageTagsDialog
import com.example.feldelysium.presentation.settings.components.BackgroundPickerDialog
import com.example.feldelysium.presentation.settings.components.BarIconsDialog
import com.example.feldelysium.presentation.settings.components.FeldBackgroundRow
import com.example.feldelysium.presentation.settings.components.PlaybackUiTextDialog

private const val PlaybackFontName = "Sina Nova Regular"

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalContext.current as ComponentActivity
    val container = (activity.application as FeldApp).container

    val vm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container),
    )
    val settings by vm.state.collectAsStateWithLifecycle()
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current
    val toast = rememberFeldToastState()

    val packVm: SpeakerPackViewModel = viewModel()
    val pack by packVm.state.collectAsStateWithLifecycle()
    val speakers by packVm.speakers.collectAsStateWithLifecycle()
    val tags by packVm.tags.collectAsStateWithLifecycle()

    var showPlaybackUiText by remember { mutableStateOf(false) }
    var showBackground by remember { mutableStateOf(false) }
    var showTags by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    var showBarIcons by remember { mutableStateOf(false) }

    BackHandler(enabled = !leaving) { leaving = true }
    LaunchedEffect(leaving) {
        if (leaving) {
            withFrameNanos { }
            withFrameNanos { }
            onNavigateBack()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        uri?.let { packVm.export(activity.contentResolver.openOutputStream(it)) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { packVm.prepareImport(activity.contentResolver.openInputStream(it)) }
    }

    pack.message?.let { m ->
        LaunchedEffect(m) {
            toast.show(m)
            packVm.consumeMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            FeldScreenHeader(
                title = "App settings",
                eyebrow = "Settings",
                subtitle = "Tune your scenes. Keep the vibe.",
                onNav = { leaving = true },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .testTag("settings_scroll"),
            ) {
                val lockedStamp = "LOCKED"

                FeldSettingsSection(title = "Playback") {
                    FeldSettingSwitchRow(
                        icon = FeldIconId.Aspect,
                        title = "Cinema frame",
                        subtitle = "Keep playback inside a 16:9 frame",
                        checked = settings.lockAspectRatio16x9,
                        onCheckedChange = {
                            eggs?.report(EggEvent.SwitchToggle, "Cinema frame")
                            vm.dispatch(SettingsEvent.SetLockAspectRatio16x9(it))
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingRow(
                        icon = FeldIconId.Grid,
                        title = "Status line",
                        subtitle = "Reals, time and day",
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            showPlaybackUiText = true
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingRow(
                        icon = FeldIconId.Settings,
                        title = "Panel icons",
                        subtitle = "Choose which bar icons are highlighted",
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            showBarIcons = true
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingSwitchRow(
                        icon = FeldIconId.Play,
                        title = "Autoplay",
                        subtitle = "Scroll lines automatically",
                        checked = settings.autoPlay,
                        onCheckedChange = { vm.dispatch(SettingsEvent.SetAutoPlay(it)) },
                    )
                    FeldSettingsDivider()
                    FeldSettingValueRow(
                        icon = FeldIconId.Timer,
                        title = "Autoplay speed",
                        subtitle = "How long each line stays on screen",
                        value = settings.autoPlaySpeed.label,
                        enabled = settings.autoPlay,
                        stampText = lockedStamp,
                        onClick = {
                            haptics.tick()
                            vm.dispatch(SettingsEvent.SetAutoPlaySpeed(settings.autoPlaySpeed.next()))
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingSwitchRow(
                        icon = FeldIconId.PlaybackText,
                        title = "Continue button",
                        subtitle = "Show Continue button",
                        checked = settings.showContinueButton,
                        onCheckedChange = { vm.dispatch(SettingsEvent.ShowContinueButton(it)) },
                    )
                    FeldSettingsDivider()
                    FeldSettingValueRow(
                        icon = FeldIconId.DialogueStyle,
                        title = "Typeface",
                        subtitle = "Dialogue text in playback",
                        value = PlaybackFontName,
                        onClick = { /* TODO: font picker dialog */ },
                    )
                }

                FeldSettingsSection(title = "Video export") {
                    FeldSettingSwitchRow(
                        icon = FeldIconId.Export,
                        title = "Record playback",
                        subtitle = "Playback button records the screen to a video",
                        checked = settings.exportEnabled,
                        onCheckedChange = { vm.dispatch(SettingsEvent.SetExportEnabled(it)) },
                    )
                    FeldSettingsDivider()
                    val audioSupported = Build.VERSION.SDK_INT >= 29
                    FeldSettingSwitchRow(
                        icon = FeldIconId.Sound,
                        title = "Record sound",
                        subtitle = if (audioSupported) "App sound goes into the video" else "Needs Android 10 or newer",
                        checked = settings.exportAudio && audioSupported,
                        onCheckedChange = { vm.dispatch(SettingsEvent.SetExportAudio(it)) },
                        enabled = settings.exportEnabled && audioSupported,
                        stampText = lockedStamp,
                    )
                }

                FeldSettingsSection(title = "Background") {
                    FeldBackgroundRow(
                        settings = settings,
                        currentLabel = backgroundLabel(settings),
                        freezeVideo = leaving,
                        onClick = {
                            haptics.tick()
                            showBackground = true
                        },
                    )
                }

                FeldSettingsSection(title = "Speakers") {
                    FeldSettingRow(
                        icon = FeldIconId.Export,
                        title = "Export speakers",
                        subtitle = "Save your speakers and tags to a file",
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            exportLauncher.launch("speakers.feldpack")
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingRow(
                        icon = FeldIconId.Import,
                        title = "Import speakers",
                        subtitle = "Add speakers and tags from a pack",
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            importLauncher.launch(arrayOf("*/*"))
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingRow(
                        icon = FeldIconId.Palette,
                        title = "Tags",
                        subtitle = "Rename, recolor or delete",
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            showTags = true
                        },
                    )
                    FeldSettingsDivider()
                    FeldSettingValueRow(
                        icon = FeldIconId.Palette,
                        title = "Author's samples",
                        subtitle = when (settings.samplesMode) {
                            SamplesMode.AUTO ->
                                if (settings.packImported) {
                                    "Pack imported, samples are hidden"
                                } else {
                                    "They'll hide after your first pack import"
                                }

                            SamplesMode.ALWAYS_SHOW -> "Always visible in the picker"

                            SamplesMode.ALWAYS_HIDE -> "Always hidden"
                        },
                        value = when (settings.samplesMode) {
                            SamplesMode.AUTO -> "Auto"
                            SamplesMode.ALWAYS_SHOW -> "Show"
                            SamplesMode.ALWAYS_HIDE -> "Hide"
                        },
                        onClick = {
                            haptics.tick()
                            val next = when (settings.samplesMode) {
                                SamplesMode.AUTO -> SamplesMode.ALWAYS_SHOW
                                SamplesMode.ALWAYS_SHOW -> SamplesMode.ALWAYS_HIDE
                                SamplesMode.ALWAYS_HIDE -> SamplesMode.AUTO
                            }
                            vm.dispatch(SettingsEvent.SetSamplesMode(next))
                        },
                    )
                }

                FeldSettingsSection(title = "Coming soon") {
                    SoonTap("Lock landscape") {
                        FeldSettingSwitchRow(
                            icon = FeldIconId.Aspect,
                            title = "Lock landscape",
                            subtitle = "Lock it",
                            checked = false,
                            onCheckedChange = {},
                            enabled = false,
                        )
                    }
                }
            }
        }

        FeldToastHost(
            toast,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }

    if (showPlaybackUiText) {
        PlaybackUiTextDialog(
            money = settings.playbackMoney,
            time = settings.playbackTime,
            day = settings.playbackDay,
            onMoneyChange = { vm.dispatch(SettingsEvent.SetPlaybackMoney(it)) },
            onTimeChange = { vm.dispatch(SettingsEvent.SetPlaybackTime(it)) },
            onDayChange = { vm.dispatch(SettingsEvent.SetPlaybackDay(it)) },
            onReset = {
                haptics.thud()
                if (settings.playbackTextIsDefault()) eggs?.report(EggEvent.ResetUnchanged)
                vm.dispatch(SettingsEvent.ResetPlaybackUiText)
            },
            onDismiss = { showPlaybackUiText = false },
        )
    }

    if (showBackground) {
        BackgroundPickerDialog(
            settings = settings,
            onKind = { vm.dispatch(SettingsEvent.SetBackgroundKind(it)) },
            onColor = { vm.dispatch(SettingsEvent.SetBackgroundColor(it)) },
            onMedia = { uri, isVideo ->
                vm.dispatch(SettingsEvent.SetBackgroundMedia(uri, isVideo))
            },
            onVideoSound = { vm.dispatch(SettingsEvent.SetBackgroundVideoSound(it)) },
            onVideoFill = { vm.dispatch(SettingsEvent.SetBackgroundVideoFill(it)) },
            onReset = {
                haptics.thud()
                if (settings.backgroundIsDefault()) eggs?.report(EggEvent.ResetUnchanged)
                vm.dispatch(SettingsEvent.ResetBackground)
            },
            onDismiss = { showBackground = false },
        )
    }

    if (showTags) {
        ManageTagsDialog(
            tags = tags,
            speakers = speakers,
            onSave = packVm::saveTag,
            onDelete = packVm::deleteTag,
            onDismiss = { showTags = false },
        )
    }

    if (showBarIcons) {
        BarIconsDialog(
            icons = settings.barIcons,
            inventory = settings.inventoryIcons,
            onCycleIcon = { slot ->
                haptics.tick()
                val current = settings.barIcons[slot] ?: slot.defaultState
                vm.dispatch(SettingsEvent.SetBarIconState(slot, current.next()))
            },
            onCycleInventory = { slot ->
                haptics.tick()
                val current = settings.inventoryIcons[slot] ?: slot.defaultIcon
                vm.dispatch(SettingsEvent.SetInventoryIcon(slot, current.next()))
            },
            onReset = {
                haptics.thud()
                vm.dispatch(SettingsEvent.ResetBarIcons)
            },
            onDismiss = { showBarIcons = false },
        )
    }

    pack.pending?.let { pending ->
        ImportConflictDialog(
            preview = pending.preview,
            onConfirm = packVm::confirmImport,
            onDismiss = packVm::cancelImport,
        )
    }
}

private fun backgroundLabel(settings: AppSettings): String = when {
    settings.backgroundKind == BackgroundKind.COLOR -> "Color"
    settings.backgroundMediaUri.isBlank() -> "Default"
    else -> "Custom"
}

@Composable
private fun SoonTap(title: String, content: @Composable () -> Unit) {
    val eggs = LocalEasterEggs.current
    Box(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { eggs?.report(EggEvent.SoonTap, title) },
    ) { content() }
}

private fun AppSettings.playbackTextIsDefault(): Boolean {
    val d = AppSettings()
    return playbackMoney == d.playbackMoney &&
        playbackTime == d.playbackTime &&
        playbackDay == d.playbackDay
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
