package com.example.feldelysium.presentation.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.designsystem.components.FeldScreenHeader
import com.example.feldelysium.designsystem.components.rememberFeldHaptics
import com.example.feldelysium.designsystem.components.settings.FeldBackgroundRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.settings.FeldSettingsSection
import com.example.feldelysium.eggs.LocalEasterEggs

private const val PlaybackFontName = "Sina Nova Regular"

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    val vm: SettingsViewModel = viewModel(activity)
    val settings by vm.state.collectAsStateWithLifecycle()
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current
    var showPlaybackUiText by remember { mutableStateOf(false) }
    var showBackground by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    BackHandler(enabled = !leaving) { leaving = true }
    LaunchedEffect(leaving) {
        if (leaving) {
            withFrameNanos { }
            withFrameNanos { }
            onNavigateBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        FeldScreenHeader(
            title = "App settings",
            eyebrow = "Settings",
            subtitle = "Tune your scenes. Keep the vibe.",
            onNav = { leaving = true },
            seed = 4,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            FeldSettingsSection(title = "Playback", seed = 11) {
                FeldSettingSwitchRow(
                    icon = Icons.Filled.AspectRatio,
                    title = "Cinema frame",
                    subtitle = "Keep playback inside a 16:9 frame",
                    checked = settings.lockAspectRatio16x9,
                    onCheckedChange = {
                        eggs?.report("switch.toggle", "Cinema frame")
                        vm.dispatch(SettingsEvent.SetLockAspectRatio16x9(it))
                    },
                    seed = 21,
                )
                FeldSettingsDivider(seed = 101)
                FeldSettingRow(
                    icon = Icons.Filled.ViewDay,
                    title = "Status line",
                    subtitle = "Reals, time and day",
                    showChevron = true,
                    onClick = { haptics.tick(); showPlaybackUiText = true },
                )
                FeldSettingsDivider(seed = 102)
                FeldSettingValueRow(
                    icon = Icons.Filled.TextFields,
                    title = "Typeface",
                    subtitle = "Dialogue text in playback",
                    value = PlaybackFontName,
                    onClick = { /* TODO: font picker dialog */ },
                )
            }

            FeldSettingsSection(title = "Background", seed = 12) {
                FeldBackgroundRow(
                    settings = settings,
                    currentLabel = backgroundLabel(settings),
                    freezeVideo = leaving,
                    onClick = { haptics.tick(); showBackground = true },
                )
            }

            FeldSettingsSection(title = "Coming soon", seed = 13) {
                SoonTap("Autoplay") {
                    FeldSettingSwitchRow(
                        icon = Icons.Filled.PlayArrow,
                        title = "Autoplay",
                        subtitle = "Play scene automatically",
                        checked = false,
                        onCheckedChange = {},
                        enabled = false,
                        seed = 22,
                    )
                }

                FeldSettingsDivider(seed = 104)

                SoonTap("Line delay") {
                    FeldSettingValueRow(
                        icon = Icons.Filled.Timer,
                        title = "Line delay",
                        subtitle = "Time between lines (seconds)",
                        value = "x",
                        enabled = false,
                    )
                }

                FeldSettingsDivider(seed = 105)

                SoonTap("Status icons") {
                    FeldSettingRow(
                        icon = Icons.Filled.Tune,
                        title = "Status icons",
                        subtitle = "Tap the icons right in playback",
                        enabled = false,
                    )
                }
            }
        }
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
                if (settings.playbackTextIsDefault()) eggs?.report("settings.reset", "nochange")
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
                vm.dispatch(
                    SettingsEvent.SetBackgroundMedia(
                        uri,
                        isVideo
                    )
                )
            },
            onVideoSound = { vm.dispatch(SettingsEvent.SetBackgroundVideoSound(it)) },
            onVideoFill = { vm.dispatch(SettingsEvent.SetBackgroundVideoFill(it)) },
            onReset = {
                haptics.thud()
                if (settings.backgroundIsDefault()) eggs?.report("settings.reset", "nochange")
                vm.dispatch(SettingsEvent.ResetBackground)
            },
            onDismiss = { showBackground = false },
        )
    }
}

private fun backgroundLabel(settings: AppSettings): String = when {
    settings.backgroundKind == BackgroundKind.COLOR -> "Color"
    settings.backgroundMediaUri.isBlank() -> "Default"
    else -> "Custom"
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
        ) { eggs?.report("soon.tap", title) }
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
