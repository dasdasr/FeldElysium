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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.FeldApp
import com.example.feldelysium.R
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.data.settings.SamplesMode
import com.example.feldelysium.data.settings.TapMode
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingStatus
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
import com.example.feldelysium.presentation.settings.components.LanguageDialog
import com.example.feldelysium.presentation.settings.components.PlaybackFontDialog
import com.example.feldelysium.presentation.settings.components.PlaybackUiTextDialog
import com.example.feldelysium.theme.FONT_CUSTOM
import com.example.feldelysium.theme.fontDir
import com.example.feldelysium.theme.prettyFontName
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    val container = (activity.application as FeldApp).container

    val vm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container, activity.fontDir()),
    )
    val scope = rememberCoroutineScope()
    var showFont by remember { mutableStateOf(false) }
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
    var showLanguage by remember { mutableStateOf(false) }

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

    pack.message?.let { message ->
        LaunchedEffect(message) {
            toast.show(message)
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
                title = stringResource(R.string.settings_title),
                eyebrow = stringResource(R.string.settings_eyebrow),
                subtitle = stringResource(R.string.settings_subtitle),
                onNav = { leaving = true },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .testTag("settings_scroll"),
            ) {
                FeldSettingsSection(
                    title = stringResource(R.string.settings_playback_section),
                ) {
                    val cinemaFrameTitle =
                        stringResource(R.string.settings_cinema_frame_title)

                    FeldSettingSwitchRow(
                        icon = FeldIconId.Cinema,
                        title = cinemaFrameTitle,
                        subtitle = stringResource(R.string.settings_cinema_frame_subtitle),
                        checked = settings.lockAspectRatio16x9,
                        onCheckedChange = {
                            eggs?.report(EggEvent.SwitchToggle, cinemaFrameTitle)
                            vm.dispatch(SettingsEvent.SetLockAspectRatio16x9(it))
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
                            showPlaybackUiText = true
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
                            showBarIcons = true
                        },
                    )

                    FeldSettingsDivider()

                    FeldSettingSwitchRow(
                        icon = FeldIconId.Autoplay,
                        title = stringResource(R.string.settings_autoplay_title),
                        subtitle = stringResource(R.string.settings_autoplay_subtitle),
                        checked = settings.autoPlay,
                        onCheckedChange = {
                            vm.dispatch(SettingsEvent.SetAutoPlay(it))
                        },
                    )

                    FeldSettingsDivider()

                    FeldSettingValueRow(
                        icon = FeldIconId.Speed,
                        title = stringResource(R.string.settings_autoplay_speed_title),
                        subtitle = stringResource(R.string.settings_autoplay_speed_subtitle),
                        value = settings.autoPlaySpeed.label,
                        status = if (settings.autoPlay) {
                            null
                        } else {
                            FeldSettingStatus.LOCK
                        },
                        onClick = {
                            haptics.tick()
                            vm.dispatch(
                                SettingsEvent.SetAutoPlaySpeed(
                                    settings.autoPlaySpeed.next(),
                                ),
                            )
                        },
                    )

                    FeldSettingsDivider()

                    FeldSettingSwitchRow(
                        icon = FeldIconId.PlaybackText,
                        title = stringResource(R.string.settings_continue_button_title),
                        subtitle = stringResource(R.string.settings_continue_button_subtitle),
                        checked = settings.showContinueButton,
                        onCheckedChange = {
                            vm.dispatch(SettingsEvent.ShowContinueButton(it))
                        },
                    )

                    FeldSettingsDivider()

                    FeldSettingValueRow(
                        icon = FeldIconId.Play,
                        title = stringResource(R.string.settings_advance_dialogue_title),
                        subtitle = when (settings.tapMode) {
                            TapMode.WHOLE_SCREEN ->
                                stringResource(R.string.settings_advance_whole_screen)

                            TapMode.CONTINUE_BUTTON ->
                                stringResource(R.string.settings_advance_continue_button)

                            TapMode.NOWHERE ->
                                stringResource(R.string.settings_advance_autoplay_only)
                        },
                        status = if (settings.showContinueButton) {
                            null
                        } else {
                            FeldSettingStatus.LOCK
                        },
                        value = settings.tapMode.localizedLabel(),
                        onClick = {
                            haptics.tick()
                            vm.dispatch(
                                SettingsEvent.SetTapMode(
                                    settings.tapMode.next(settings.autoPlay),
                                ),
                            )
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
                            showFont = true
                        },
                    )
                }

                FeldSettingsSection(
                    title = stringResource(R.string.settings_video_export_section),
                ) {
                    FeldSettingSwitchRow(
                        icon = FeldIconId.Video,
                        title = stringResource(R.string.settings_record_playback_title),
                        subtitle = stringResource(R.string.settings_record_playback_subtitle),
                        checked = settings.exportEnabled,
                        onCheckedChange = {
                            vm.dispatch(SettingsEvent.SetExportEnabled(it))
                        },
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
                        onCheckedChange = {
                            vm.dispatch(SettingsEvent.SetExportAudio(it))
                        },
                        status = if (settings.exportEnabled && audioSupported) {
                            null
                        } else {
                            FeldSettingStatus.LOCK
                        },
                    )
                }

                FeldSettingsSection(
                    title = stringResource(R.string.settings_background_section),
                ) {
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

                FeldSettingsSection(
                    title = stringResource(R.string.settings_speakers_section),
                ) {
                    FeldSettingRow(
                        icon = FeldIconId.Export,
                        title = stringResource(R.string.settings_export_speakers_title),
                        subtitle = stringResource(R.string.settings_export_speakers_subtitle),
                        showChevron = true,
                        onClick = {
                            haptics.tick()
                            exportLauncher.launch("speakers.feldpack")
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
                            importLauncher.launch(arrayOf("*/*"))
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
                            showTags = true
                        },
                    )

                    FeldSettingsDivider()

                    FeldSettingValueRow(
                        icon = FeldIconId.AuthorSamples,
                        title = stringResource(R.string.settings_author_samples_title),
                        subtitle = when (settings.samplesMode) {
                            SamplesMode.AUTO -> {
                                if (settings.packImported) {
                                    stringResource(R.string.settings_samples_imported_hidden)
                                } else {
                                    stringResource(R.string.settings_samples_hide_after_import)
                                }
                            }

                            SamplesMode.ALWAYS_SHOW ->
                                stringResource(R.string.settings_samples_always_visible)

                            SamplesMode.ALWAYS_HIDE ->
                                stringResource(R.string.settings_samples_always_hidden)
                        },
                        value = when (settings.samplesMode) {
                            SamplesMode.AUTO ->
                                stringResource(R.string.settings_samples_auto)

                            SamplesMode.ALWAYS_SHOW ->
                                stringResource(R.string.settings_samples_show)

                            SamplesMode.ALWAYS_HIDE ->
                                stringResource(R.string.settings_samples_hide)
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

                FeldSettingsSection(
                    title = stringResource(R.string.settings_language_title),
                ) {
                    val language = AppLanguage.current()

                    FeldSettingValueRow(
                        icon = FeldIconId.Language,
                        title = stringResource(R.string.settings_language_title),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                        value = when (language) {
                            AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
                            AppLanguage.ENGLISH -> "English"
                            AppLanguage.BELARUSIAN -> "Беларуская"
                        },
                        onClick = {
                            haptics.tick()
                            showLanguage = true
                        },
                    )
                }

                FeldSettingsSection(
                    title = stringResource(R.string.settings_coming_soon_section),
                ) {
                    val lockLandscapeTitle =
                        stringResource(R.string.settings_lock_landscape_title)

                    SoonTap(lockLandscapeTitle) {
                        FeldSettingSwitchRow(
                            icon = FeldIconId.Cinema,
                            title = lockLandscapeTitle,
                            subtitle = stringResource(R.string.settings_lock_landscape_subtitle),
                            checked = false,
                            onCheckedChange = {},
                            status = FeldSettingStatus.SOON,
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
            onMoneyChange = {
                vm.dispatch(SettingsEvent.SetPlaybackMoney(it))
            },
            onTimeChange = {
                vm.dispatch(SettingsEvent.SetPlaybackTime(it))
            },
            onDayChange = {
                vm.dispatch(SettingsEvent.SetPlaybackDay(it))
            },
            onReset = {
                haptics.thud()
                if (settings.playbackTextIsDefault()) {
                    eggs?.report(EggEvent.ResetUnchanged)
                }
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
            onVideoSound = {
                vm.dispatch(SettingsEvent.SetBackgroundVideoSound(it))
            },
            onVideoFill = {
                vm.dispatch(SettingsEvent.SetBackgroundVideoFill(it))
            },
            onReset = {
                haptics.thud()
                if (settings.backgroundIsDefault()) {
                    eggs?.report(EggEvent.ResetUnchanged)
                }
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

    if (showFont) {
        PlaybackFontDialog(
            settings = settings,
            onSelect = vm::selectFont,
            onReset = { vm.selectFont("") },
            onUpload = { uri ->
                scope.launch {
                    if (!vm.importFont(uri)) {
                        toast.show(
                            activity.getString(R.string.settings_font_invalid),
                        )
                    }
                }
            },
            onDismiss = { showFont = false },
        )
    }

    if (showLanguage) {
        LanguageDialog(
            current = AppLanguage.current(),
            onSelect = { AppLanguage.apply(it) },
            onDismiss = { showLanguage = false },
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

@Composable
private fun backgroundLabel(settings: AppSettings): String = when {
    settings.backgroundKind == BackgroundKind.COLOR ->
        stringResource(R.string.settings_background_color)

    settings.backgroundMediaUri.isBlank() ->
        stringResource(R.string.settings_background_default)

    else ->
        stringResource(R.string.settings_background_custom)
}

@Composable
private fun SoonTap(
    title: String,
    content: @Composable () -> Unit,
) {
    val eggs = LocalEasterEggs.current

    Box(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) {
            eggs?.report(EggEvent.SoonTap, title)
        },
    ) {
        content()
    }
}

private fun AppSettings.playbackTextIsDefault(): Boolean {
    val default = AppSettings()
    return playbackMoney == default.playbackMoney &&
            playbackTime == default.playbackTime &&
            playbackDay == default.playbackDay
}

private fun AppSettings.backgroundIsDefault(): Boolean {
    val default = AppSettings()
    return backgroundKind == default.backgroundKind &&
            backgroundColorArgb == default.backgroundColorArgb &&
            backgroundMediaUri == default.backgroundMediaUri &&
            backgroundMediaIsVideo == default.backgroundMediaIsVideo &&
            backgroundVideoSound == default.backgroundVideoSound &&
            backgroundVideoFill == default.backgroundVideoFill
}

@Composable
private fun TapMode.localizedLabel(): String = when (this) {
    TapMode.WHOLE_SCREEN ->
        stringResource(R.string.settings_tap_mode_whole_screen)

    TapMode.CONTINUE_BUTTON ->
        stringResource(R.string.settings_tap_mode_continue_button)

    TapMode.NOWHERE ->
        stringResource(R.string.settings_tap_mode_nowhere)
}
