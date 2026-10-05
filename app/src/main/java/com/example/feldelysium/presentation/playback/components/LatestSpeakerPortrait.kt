package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.R
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers

@Preview
@Composable
private fun LatestSpeakerPortraitPreview() {
    LatestSpeakerPortrait(
        speaker = PredefinedSpeakers.STEBAN,
        portraitWidth  = 177.dp,
        portraitHeight = 250.dp,
    )
}

@Composable
fun LatestSpeakerPortrait(
    speaker: Speaker?,
    portraitWidth: Dp,
    portraitHeight: Dp,
    modifier: Modifier = Modifier
) {
    if (speaker == null) return
    if (speaker.customIconUri == null && speaker.iconResId == null) return

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.ic_measurehead), //mock
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp)
                .size(width = portraitWidth, height = portraitHeight)
        ) {
            when {
                speaker.customIconUri != null -> AsyncImage(
                    model = speaker.customIconUri,
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                speaker.iconResId != null -> Image(
                    painter = painterResource(id = speaker.iconResId),
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}