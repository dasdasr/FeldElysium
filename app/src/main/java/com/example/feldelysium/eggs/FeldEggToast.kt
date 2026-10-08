package com.example.feldelysium.eggs

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.rememberFeldHaptics
import com.example.feldelysium.designsystem.components.speaker.FeldSpeakerDiamond
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import kotlinx.coroutines.delay

val LocalEasterEggs = staticCompositionLocalOf<EasterEggs?> { null }

@SuppressLint("ContextCastToActivity")
@Composable
fun ProvideEasterEggs(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val activity = LocalContext.current as ComponentActivity
    val vm: EggsViewModel = viewModel(activity)
    val eggs = vm.eggs

    LaunchedEffect(eggs) {
        eggs.awaitReady()
        eggs.report("app.open")
    }

    CompositionLocalProvider(LocalEasterEggs provides eggs) {
        Box(modifier.fillMaxSize()) {
            content()
            FeldEggToastHost(eggs, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun SuppressEasterEggs() {
    val eggs = LocalEasterEggs.current
    DisposableEffect(eggs) {
        eggs?.setSuppressed(true)
        onDispose { eggs?.setSuppressed(false) }
    }
}

@Composable
fun FeldEggToastHost(eggs: EasterEggs, modifier: Modifier = Modifier) {
    val toast by eggs.toast.collectAsStateWithLifecycle()
    val suppressed by eggs.suppressed.collectAsStateWithLifecycle()
    val haptics = rememberFeldHaptics()
    var last by remember { mutableStateOf<EasterEggs.ActiveToast?>(null) }

    LaunchedEffect(toast) { toast?.let { last = it } }

    LaunchedEffect(toast?.id) {
        val current = toast ?: return@LaunchedEffect
        haptics.confirm()
        delay(current.durationMillis)
        eggs.dismiss(current.id)
    }

    val shown = toast ?: last
    AnimatedVisibility(
        visible = toast != null && !suppressed,
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        enter = slideInVertically { -it } + fadeIn(tween(180)),
        exit = if (suppressed) ExitTransition.None
        else slideOutVertically { -it } + fadeOut(tween(160)),
    ) {
        shown?.let {
            FeldThoughtToast(
                reply = it.reply,
                seed = it.id.toInt(),
                onClick = { eggs.dismiss(it.id) },
            )
        }
    }
}

@Composable
fun FeldThoughtToast(
    reply: EggReply,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    onClick: (() -> Unit)? = null,
) {
    val skillColor = remember(reply.skill) { skillColorOf(reply.skill) }

    FeldCard(
        modifier = modifier.fillMaxWidth(),
        seed = seed,
        rough = RoughStyle(roughness = 1.dp, corner = 3.dp, tornChance = 0.15f),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        revealOnEnter = true,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            FeldSpeakerDiamond(color = skillColor, size = 26.dp, seed = seed)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = reply.header,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                    color = skillColor,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = reply.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun skillColorOf(skill: String): Color =
    PredefinedSpeakers.ALL
        .firstOrNull { it.name.equals(skill, ignoreCase = true) }
        ?.let { Color(it.colorArgb) }
        ?: FeldPalette.Muted
