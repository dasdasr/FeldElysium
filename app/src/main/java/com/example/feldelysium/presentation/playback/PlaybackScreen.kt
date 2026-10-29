package com.example.feldelysium.presentation.playback

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.FeldApp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.theme.ChromaKeyGreen
import com.example.feldelysium.designsystem.theme.ProvidePlaybackFont
import com.example.feldelysium.designsystem.theme.fontDir
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.model.StatusLine
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.domain.model.settings.TapMode
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.playback.components.ActiveCheckOverlay
import com.example.feldelysium.presentation.playback.components.ActiveDamageOverlay
import com.example.feldelysium.presentation.playback.components.DamageBackdrop
import com.example.feldelysium.presentation.playback.components.DialogueHistoryList
import com.example.feldelysium.presentation.playback.components.LatestSpeakerPortrait
import com.example.feldelysium.presentation.playback.components.PanelMotion
import com.example.feldelysium.presentation.playback.components.PlaybackBackground
import com.example.feldelysium.presentation.playback.components.PlaybackBottomBar
import com.example.feldelysium.presentation.playback.components.PlaybackPromptFooter
import com.example.feldelysium.presentation.playback.components.StageRef
import com.example.feldelysium.presentation.settings.SettingsViewModel
import com.example.feldelysium.record.RecordState
import com.example.feldelysium.record.ScreenRecordCoordinator
import com.example.feldelysium.record.ScreenRecordService
import com.example.feldelysium.record.realScreenSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("ContextCastToActivity")
@Composable
fun PlaybackScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val eggs = LocalEasterEggs.current
    var openReported by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(eggs) {
        if (eggs != null && !openReported) {
            eggs.awaitReady()
            eggs.report(EggEvent.AppOpen)
            openReported = true
        }
    }

    val activity = LocalContext.current as ComponentActivity
    val container = (activity.application as FeldApp).container

    val settingsVm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container, activity.fontDir()),
    )

    val vm: PlaybackViewModel = viewModel(
        factory = PlaybackViewModel.factory(container),
    )
    val state = vm.state.collectAsStateWithLifecycle().value
    val settings = settingsVm.state.collectAsStateWithLifecycle().value

    DisposableEffect(Unit) {
        val original = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { activity.requestedOrientation = original }
    }

    DisposableEffect(Unit) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(isLandscape) {
        if (!isLandscape) return@LaunchedEffect
        val grant = ScreenRecordCoordinator.take()
        if (grant != null) {
            delay(600.milliseconds)
            val size = activity.realScreenSize()
            ScreenRecordService.start(
                activity,
                grant.resultCode,
                grant.data,
                size.width,
                size.height,
                size.dpi,
                grant.withAudio,
            )
            val started = withTimeoutOrNull(5_000.milliseconds) {
                ScreenRecordService.state.first { it == RecordState.Recording }
            }
            if (started == null) return@LaunchedEffect
        }
        if (settings.autoPlay) vm.startAutoPlay(settings.autoPlaySpeed.factor)
    }

    val autoDone by vm.autoPlayDone.collectAsStateWithLifecycle()
    LaunchedEffect(autoDone) {
        if (autoDone && ScreenRecordService.state.value == RecordState.Recording) {
            ScreenRecordService.stop(activity)
            onNavigateBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (ScreenRecordService.state.value == RecordState.Recording) {
                ScreenRecordService.stop(activity)
            }
        }
    }

    ProvidePlaybackFont(settings) {
        PlaybackScreenContent(
            state = state,
            lockAspectRatio16x9 = settings.lockAspectRatio16x9,
            background = settings,
            money = settings.playbackMoney,
            time = settings.playbackTime,
            day = settings.playbackDay,
            onTap = { vm.dispatch(PlaybackEvent.Tap) },
            onBackClick = onNavigateBack,
            onScrollChanged = { vm.dispatch(PlaybackEvent.ScrollChanged(it)) },
            onContinue = { vm.dispatch(PlaybackEvent.ContinueClicked) },
            onChoice = { vm.dispatch(PlaybackEvent.ChoiceClicked) },
            modifier = modifier,
        )
    }
}

@Composable
fun PlaybackScreenContent(
    state: PlaybackUiState,
    onTap: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    lockAspectRatio16x9: Boolean = true,
    background: AppSettings = AppSettings(),
    money: String = "40.95",
    time: String = "16:36",
    day: String = "Day 3",
    onScrollChanged: (Boolean) -> Unit = {},
    onContinue: () -> Unit = {},
    onChoice: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (lockAspectRatio16x9) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                PlaybackStage(
                    state = state,
                    onTap = onTap,
                    onBackClick = onBackClick,
                    onContinue = onContinue,
                    onChoice = onChoice,
                    background = background,
                    money = money,
                    time = time,
                    day = day,
                    modifier = Modifier.aspectRatio(16f / 9f, matchHeightConstraintsFirst = false),
                    onScrollChanged = onScrollChanged,
                )
            }
        } else {
            PlaybackStage(
                state = state,
                onTap = onTap,
                onBackClick = onBackClick,
                onContinue = onContinue,
                onChoice = onChoice,
                onScrollChanged = onScrollChanged,
                background = background,
                money = money, time = time, day = day,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlaybackStage(
    state: PlaybackUiState,
    onTap: () -> Unit,
    onBackClick: () -> Unit,
    background: AppSettings,
    money: String,
    time: String,
    day: String,
    modifier: Modifier = Modifier,
    onScrollChanged: (Boolean) -> Unit = {},
    onContinue: () -> Unit = {},
    onChoice: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
    ) {
        val tapeWidth = maxWidth * StageRef.TAPE_WIDTH
        val tapeRightMargin = maxWidth * StageRef.TAPE_RIGHT_MARGIN
        val portraitLeft = maxWidth * StageRef.PORTRAIT_LEFT
        val portraitTop = maxHeight * StageRef.PORTRAIT_TOP
        val portraitWidth = maxWidth * StageRef.PORTRAIT_WIDTH
        val portraitHeight = maxHeight * StageRef.PORTRAIT_HEIGHT
        val portraitBackdropInset = portraitWidth * StageRef.PORTRAIT_BACKDROP_INSET_RATIO

        val panelShift by animateFloatAsState(
            targetValue = if (state.isPanelOpen) 0f else 1f,
            animationSpec = tween(PanelMotion.SLIDE_MS, easing = FastOutSlowInEasing),
            label = "panelShift",
        )

        PlaybackBackground(
            settings = background,
            modifier = Modifier.matchParentSize(),
        )

        DamageBackdrop(overlay = state.damageOverlay, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationX = panelShift * size.width },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(tapeWidth + tapeRightMargin)
                    .align(Alignment.CenterEnd),
            ) {
                DialogueHistoryList(
                    entries = state.revealedEntries,
                    isCheckRolling = state.isCheckRolling,
                    modifier = Modifier.fillMaxHeight().width(tapeWidth).align(Alignment.CenterStart),
                    onScrollActive = onScrollChanged,
                    dragToScroll = background.showContinueButton &&
                        background.tapMode == TapMode.CONTINUE_BUTTON,
                    footer = { metrics -> PlaybackPromptFooter(state.prompt, metrics, onContinue, onChoice) },
                )
            }

            if (!state.isCheckRolling) {
                LatestSpeakerPortrait(
                    speaker = state.revealedEntries.lastOrNull()?.speaker,
                    modifier = Modifier
                        .offset(x = portraitLeft - portraitBackdropInset, y = portraitTop - portraitBackdropInset)
                        .size(
                            width = portraitWidth + portraitBackdropInset * 2,
                            height = portraitHeight + portraitBackdropInset * 2,
                        ),
                )
            }
        }

        ActiveCheckOverlay(
            overlay = state.checkOverlay,
            modifier = Modifier.matchParentSize(),
        )

        ActiveDamageOverlay(overlay = state.damageOverlay, modifier = Modifier.matchParentSize())

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(R.string.common_back),
                tint = ChromaKeyGreen.copy(alpha = 0.0f),
            )
        }

        PlaybackBottomBar(
            icons = background.barIcons,
            inventory = background.inventoryIcons,
            status = StatusLine(money, time, day),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(maxWidth * StageRef.BOTTOM_BAR_WIDTH),
        )
    }
}

@Preview(
    name = "Playback",
    widthDp = 891,
    heightDp = 411,
    device = Devices.PIXEL_9,
)
@Composable
private fun PlaybackScreenPreview() {
    val skillSpeaker = PredefinedSpeakers.WORLDWRIGHT
    val npcSpeaker = Speaker(
        id = "kim",
        name = "Kim Kitsuragi",
        colorArgb = 0xFF_B5_A6_42L,
        type = SpeakerType.NPC,
    )

    PlaybackScreenContent(
        state = PlaybackUiState(
            allEntries = emptyList(),
            revealedEntries = listOf(
                DialogueEntry(
                    speaker = npcSpeaker,
                    text = "We should get going. The precinct won't wait forever.",
                ),
                DialogueEntry(
                    speaker = skillSpeaker,
                    text = "He's right. You've been standing here for eleven minutes.",
                ),
                DialogueEntry(
                    speaker = npcSpeaker,
                    text = "Detective? Are you listening?",
                ),
            ),
            isFinished = false,
        ),
        onTap = {},
        onBackClick = {},
    )
}
