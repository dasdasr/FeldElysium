package com.example.feldelysium.presentation.playback

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.domain.model.AttributeGroup
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.presentation.playback.components.DialogueHistoryList
import com.example.feldelysium.presentation.playback.components.LatestSpeakerPortrait
import com.example.feldelysium.presentation.playback.components.TapeBackground
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

@SuppressLint("ContextCastToActivity")
@Composable
fun PlaybackScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as ComponentActivity
    val sceneVm: SceneViewModel = viewModel(activity)
    val vm: PlaybackViewModel = viewModel(factory = PlaybackViewModel.factory(sceneVm))
    val state = vm.state.collectAsStateWithLifecycle().value

    DisposableEffect(Unit) {
        val original = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { activity.requestedOrientation = original }
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
        onTap = { vm.dispatch(PlaybackEvent.Tap) },
        onBackClick = { vm.dispatch(PlaybackEvent.NavigateBack) },
        modifier = modifier
    )
}

@Composable
fun PlaybackScreenContent(
    state: PlaybackUiState,
    onTap: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.revealedEntries.size) {
        if (state.revealedEntries.isNotEmpty()) listState.animateScrollToItem(0)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ChromaKeyGreen)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
    ) {
        val tapeWidth = maxWidth * (TAPE_WIDTH_PX / REFERENCE_WIDTH_PX)
        val tapeRightMargin = maxWidth * (TAPE_RIGHT_MARGIN_PX / REFERENCE_WIDTH_PX)
        val portraitLeft = maxWidth * (PORTRAIT_LEFT_PX / REFERENCE_WIDTH_PX)
        val portraitTop = maxHeight * (PORTRAIT_TOP_PX / REFERENCE_HEIGHT_PX)
        val portraitWidth = maxWidth * (PORTRAIT_WIDTH_PX / REFERENCE_WIDTH_PX)
        val portraitHeight = maxHeight * (PORTRAIT_HEIGHT_PX / REFERENCE_HEIGHT_PX)
        val portraitBackdropInset = portraitWidth * PORTRAIT_BACKDROP_INSET_RATIO

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(tapeWidth + tapeRightMargin)
                .align(Alignment.CenterEnd)
        ) {
            TapeBackground(
                scrollProgress = state.revealedEntries.size.toFloat(),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(tapeWidth)
                    .align(Alignment.CenterStart)
            )
            DialogueHistoryList(
                entries = state.revealedEntries,
                listState = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(tapeWidth)
                    .align(Alignment.CenterStart)
            )
        }

        LatestSpeakerPortrait(
            speaker = state.revealedEntries.lastOrNull()?.speaker,
            portraitWidth = portraitWidth,
            portraitHeight = portraitHeight,
            modifier = Modifier
                .offset(
                    x = portraitLeft - portraitBackdropInset,
                    y = portraitTop - portraitBackdropInset
                )
                .size(
                    width = portraitWidth + portraitBackdropInset * 2,
                    height = portraitHeight + portraitBackdropInset * 2
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ChromaKeyGreen.copy(alpha = 0.0f))
        }
    }
}

@Preview(
    name = "Playback",
    widthDp = 891,
    heightDp = 411,
    device = Devices.PIXEL_9
)
@Composable
private fun PlaybackScreenPreview() {
    val skillSpeaker = Speaker(
        id = "logic",
        name = "Logic",
        colorArgb = 0xFF_42_A6_B5L,
        type = SpeakerType.SKILL,
        attributeGroup = AttributeGroup.INTELLECT
    )
    val npcSpeaker = Speaker(
        id = "kim",
        name = "Kim Kitsuragi",
        colorArgb = 0xFF_B5_A6_42L,
        type = SpeakerType.NPC
    )

    PlaybackScreenContent(
        state = PlaybackUiState(
            allEntries = emptyList(),
            revealedEntries = listOf(
                DialogueEntry(
                    speaker = npcSpeaker,
                    text = "We should get going. The precinct won't wait forever."
                ),
                DialogueEntry(
                    speaker = skillSpeaker,
                    text = "He's right. You've been standing here for eleven minutes."
                ),
                DialogueEntry(
                    speaker = npcSpeaker,
                    text = "Detective? Are you listening?"
                )
            ),
            isFinished = false
        ),
        onTap = {},
        onBackClick = {}
    )
}