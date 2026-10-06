package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.presentation.playback.PlaybackMotion

private val BodySize = 12.sp
private val BodyLineHeight = 16.sp
private val NameLetterSpacing = 0.4.sp
private val HangingIndent = 14.sp
private val BodyColor = Color(0xFF_E8_D8_C0)

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DialogueEntryItemPreview() {
    DialogueEntryItem(
        entry = DialogueEntry(
            speaker = PredefinedSpeakers.STEBAN,
            text = "Go-go Gyro",
        ),
        isLatest = true,
    )
}

@Composable
fun DialogueEntryItem(
    entry: DialogueEntry,
    isLatest: Boolean,
    modifier: Modifier = Modifier,
) {
    val alpha = remember(entry.id) { Animatable(0f) }
    LaunchedEffect(isLatest) {
        val target = if (isLatest) 1f else PlaybackMotion.PREVIOUS_LINES_ALPHA
        val duration = if (isLatest) PlaybackMotion.TEXT_FADE_IN_MS else PlaybackMotion.PREVIOUS_DIM_MS
        alpha.animateTo(target, tween(durationMillis = duration))
    }
    val animatedAlpha = alpha.value

    val bodyStyle = MaterialTheme.typography.bodyMedium
    val speakerColor = Color(entry.speaker.colorArgb)
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = speakerColor,
                    fontSize = BodySize,
                    letterSpacing = NameLetterSpacing,
                )
            ) {
                append(entry.speaker.name.uppercase())
                append(" – ")
            }
            withStyle(
                SpanStyle(
                    color = BodyColor,
                    fontSize = BodySize,
                )
            ) {
                append(entry.text)
            }
        },
        modifier = modifier
            .graphicsLayer { this.alpha = animatedAlpha },
        style = bodyStyle.copy(
            color = BodyColor,
            fontSize = BodySize,
            lineHeight = BodyLineHeight,
            textIndent = TextIndent(firstLine = 0.sp, restLine = HangingIndent),
        ),
    )
}
