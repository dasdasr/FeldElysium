package com.example.feldelysium.presentation.playback

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.R
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.playback.components.ActiveCheckOverlay
import com.example.feldelysium.presentation.playback.components.ActiveDamageOverlay
import com.example.feldelysium.presentation.playback.components.DamageBackdrop
import com.example.feldelysium.presentation.playback.components.DialogueHistoryList
import com.example.feldelysium.presentation.playback.components.LatestSpeakerPortrait
import com.example.feldelysium.presentation.playback.components.PlaybackBackground
import com.example.feldelysium.presentation.playback.components.PlaybackStatusHud
import com.example.feldelysium.presentation.settings.SettingsViewModel
import com.example.feldelysium.presentation.shared.SceneViewModel
import com.example.feldelysium.theme.ChromaKeyGreen

private const val REFERENCE_WIDTH_PX = 1920f
private const val REFERENCE_HEIGHT_PX = 1080f
private const val TAPE_WIDTH_PX = 614f
private const val TAPE_RIGHT_MARGIN_PX = 111f
private const val PORTRAIT_LEFT_PX = 1022f
private const val PORTRAIT_TOP_PX = 232f
private const val PORTRAIT_WIDTH_PX = 177f
private const val PORTRAIT_HEIGHT_PX = 250f
private const val PORTRAIT_BACKDROP_INSET_RATIO = 0.12f
private const val BOTTOM_BAR_WIDTH_PX = 770f

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
    val sceneVm: SceneViewModel = viewModel(activity)
    val settingsVm: SettingsViewModel = viewModel(activity)
    val vm: PlaybackViewModel = viewModel(factory = PlaybackViewModel.factory(sceneVm))
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

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                PlaybackEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    PlaybackScreenContent(
        state = state,
        lockAspectRatio16x9 = settings.lockAspectRatio16x9,
        background = settings,
        money = settings.playbackMoney,
        time = settings.playbackTime,
        day = settings.playbackDay,
        onTap = { vm.dispatch(PlaybackEvent.Tap) },
        onBackClick = { vm.dispatch(PlaybackEvent.NavigateBack) },
        modifier = modifier,
    )
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
                    background = background,
                    money = money,
                    time = time,
                    day = day,
                    modifier = Modifier.aspectRatio(16f / 9f, matchHeightConstraintsFirst = false),
                )
            }
        } else {
            PlaybackStage(
                state = state,
                onTap = onTap,
                onBackClick = onBackClick,
                background = background,
                money = money,
                time = time,
                day = day,
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
) {
    BoxWithConstraints(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
    ) {
        val tapeWidth = maxWidth * (TAPE_WIDTH_PX / REFERENCE_WIDTH_PX)
        val tapeRightMargin = maxWidth * (TAPE_RIGHT_MARGIN_PX / REFERENCE_WIDTH_PX)
        val portraitLeft = maxWidth * (PORTRAIT_LEFT_PX / REFERENCE_WIDTH_PX)
        val portraitTop = maxHeight * (PORTRAIT_TOP_PX / REFERENCE_HEIGHT_PX)
        val portraitWidth = maxWidth * (PORTRAIT_WIDTH_PX / REFERENCE_WIDTH_PX)
        val portraitHeight = maxHeight * (PORTRAIT_HEIGHT_PX / REFERENCE_HEIGHT_PX)
        val portraitBackdropInset = portraitWidth * PORTRAIT_BACKDROP_INSET_RATIO

        PlaybackBackground(
            settings = background,
            modifier = Modifier.matchParentSize(),
        )

        DamageBackdrop(overlay = state.damageOverlay, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(tapeWidth + tapeRightMargin)
                .align(Alignment.CenterEnd),
        ) {
            DialogueHistoryList(
                entries = state.revealedEntries,
                isCheckRolling = state.isCheckRolling,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(tapeWidth)
                    .align(Alignment.CenterStart),
            )
        }

        if (!state.isCheckRolling) {
            LatestSpeakerPortrait(
                speaker = state.revealedEntries.lastOrNull()?.speaker,
                modifier = Modifier
                    .offset(
                        x = portraitLeft - portraitBackdropInset,
                        y = portraitTop - portraitBackdropInset,
                    )
                    .size(
                        width = portraitWidth + portraitBackdropInset * 2,
                        height = portraitHeight + portraitBackdropInset * 2,
                    ),
            )
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
                "Back",
                tint = ChromaKeyGreen.copy(alpha = 0.0f),
            )
        }

        Image(
            painter = painterResource(R.drawable.bottom_ui_bar),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(maxWidth * (BOTTOM_BAR_WIDTH_PX / REFERENCE_WIDTH_PX)),
        )

        PlaybackStatusHud(
            money = money,
            time = time,
            day = day,
            modifier = Modifier.fillMaxSize(),
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
    val skillSpeaker = PredefinedSpeakers.LOGIC
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
