package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.feldelysium.R

private const val TAPE_TEXTURE_WIDTH_PX = 614f
private const val TAPE_TEXTURE_HEIGHT_PX = 3598f
private const val TAPE_TEXTURE_ASPECT = TAPE_TEXTURE_HEIGHT_PX / TAPE_TEXTURE_WIDTH_PX
private const val AMBIENT_SCROLL_DURATION_MS = 12000
private const val ENTRY_SCROLL_FRACTION = 0.06f

@Preview
@Composable
private fun TapeBackgroundPreview() {
    TapeBackground(
        scrollProgress = 0.1f,
    )
}

@Composable
fun TapeBackground(
    scrollProgress: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val tileHeight = maxWidth * TAPE_TEXTURE_ASPECT
        val infiniteTransition = rememberInfiniteTransition(label = "tape")
        val ambientFraction by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = AMBIENT_SCROLL_DURATION_MS,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "tape_ambient"
        )
        val entryFraction = (scrollProgress * ENTRY_SCROLL_FRACTION) % 1f
        val wrappedOffset = tileHeight * ((ambientFraction + entryFraction) % 1f)
        val tileCount = (maxHeight / tileHeight).toInt() + 3
        val painter = painterResource(id = R.drawable.seamless_tape)

        for (index in 0 until tileCount) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tileHeight)
                    .offset(y = tileHeight * index - tileHeight - wrappedOffset)
            )
        }
    }
}