package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.R
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private const val PORTRAIT_PHOTO_WIDTH_FRACTION = 0.92f
private const val PORTRAIT_PHOTO_HEIGHT_FRACTION = 0.88f
private const val FRAME_SCALE_X = 1.02f
private const val FRAME_SCALE_Y = 1.02f

@Preview
@Composable
private fun LatestSpeakerPortraitPreview() {
    LatestSpeakerPortrait(
        speaker = PredefinedSpeakers.STEBAN,
        modifier = Modifier.size(width = 192.dp, height = 281.dp),
    )
}

@Composable
fun LatestSpeakerPortrait(speaker: Speaker?, modifier: Modifier = Modifier) {
    if (speaker == null) return
    if (speaker.customIconUri == null && speaker.iconResId == null) return

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.profile_frame),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = FRAME_SCALE_X
                    scaleY = FRAME_SCALE_Y
                },
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(PORTRAIT_PHOTO_WIDTH_FRACTION)
                .fillMaxHeight(PORTRAIT_PHOTO_HEIGHT_FRACTION),
        ) {
            when {
                speaker.customIconUri != null -> AsyncImage(
                    model = speaker.customIconUri,
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )

                speaker.iconResId != null -> Image(
                    painter = painterResource(id = speaker.iconResId),
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
