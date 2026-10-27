package com.example.feldelysium.presentation.playback.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.feldelysium.domain.model.settings.BarIconState

@Composable
internal fun BarGlyph(
    @DrawableRes res: Int,
    state: BarIconState,
    size: Dp,
    modifier: Modifier = Modifier,
    mirrored: Boolean = false,
) {
    Box(modifier.size(size)) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            colorFilter = ColorFilter.tint(state.tint),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { if (mirrored) scaleX = -1f },
        )
        if (state == BarIconState.NOTIFY) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.26f)
                    .clip(CircleShape)
                    .background(BarIconState.NOTIFY.tint),
            )
        }
    }
}
