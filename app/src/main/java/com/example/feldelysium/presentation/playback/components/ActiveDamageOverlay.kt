package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.presentation.playback.DamageOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DamageBackdrop(overlay: DamageOverlay?, modifier: Modifier = Modifier) {
    if (overlay == null) return
    key(overlay.entryId) {
        val a = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            a.animateTo(CheckMotion.DMG_BACKDROP_ALPHA, tween(CheckMotion.DMG_BACKDROP_IN_MS, easing = EaseOut))
            a.animateTo(0f, tween(CheckMotion.DMG_BACKDROP_OUT_MS, easing = EaseInOut))
        }
        BlendedImage(
            res = CheckAssets.Vignette,
            modifier = modifier.graphicsLayer { alpha = a.value },
        )
    }
}

@Composable
fun ActiveDamageOverlay(overlay: DamageOverlay?, modifier: Modifier = Modifier) {
    if (overlay == null) return
    key(overlay.entryId) { DamageContent(overlay.damage, modifier) }
}

@Composable
private fun DamageContent(damage: DamageEffect, modifier: Modifier = Modifier) {
    val icon = remember { Animatable(1f) }
    val textOut = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(CheckMotion.DMG_OUT_DELAY_MS.milliseconds)
        launch { icon.animateTo(0f, tween(CheckMotion.DMG_ICON_FADE_MS, easing = EaseIn)) }
        textOut.animateTo(1f, tween(CheckMotion.DMG_TEXT_OUT_MS, easing = EaseIn))
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val color = CheckAssets.damageColor(damage.type)
        val plaqueHeight = maxHeight * CheckMotion.RESULT_TEXT_HEIGHT_FRACTION
        val slidePx = with(LocalDensity.current) { maxWidth.toPx() } * CheckMotion.DMG_SLIDE_FRACTION

        val maxOverlayHeight = maxHeight
        val iconSize = maxOverlayHeight * CheckMotion.DMG_ICON_SIZE_FRACTION

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = maxOverlayHeight * CheckMotion.OVERLAY_BOTTOM_FRACTION,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(CheckAssets.damageIcon()),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        alpha = icon.value
                    },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - textOut.value
                    translationX = -textOut.value * slidePx
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ResultPlaque(CheckAssets.damageLabel(damage.type), color, plaqueHeight)
                ResultPlaque("-${damage.amount}", color, plaqueHeight * 0.7f)
            }
        }
    }
}
