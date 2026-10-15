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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private const val NAME_TRACKING_EM = 0.4f / 12f
private const val HANGING_INDENT_EM = 14f / 12f
private val BodyColor = Color(0xFF_E8_D8_C0)
private val CheckLabelColor = Color(0xFF_A1_A1_AA)

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DialogueEntryItemPreview() {
    DialogueEntryItem(
        entry = DialogueEntry(
            speaker = PredefinedSpeakers.STEBAN,
            text = "Go-go Gyro",
        ),
        isLatest = true,
        fontSize = 9.sp,
        lineHeight = 12.sp,
    )
}

@Composable
fun DialogueEntryItem(
    entry: DialogueEntry,
    isLatest: Boolean,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
) {
    val alpha = remember(entry.id) { Animatable(0f) }
    val bright = isLatest && !dimmed
    LaunchedEffect(bright) {
        val target = if (bright) 1f else PlaybackMotion.PREVIOUS_LINES_ALPHA
        val duration = if (bright) PlaybackMotion.TEXT_FADE_IN_MS else PlaybackMotion.PREVIOUS_DIM_MS
        alpha.animateTo(target, tween(durationMillis = duration))
    }
    val animatedAlpha = alpha.value

    val bodyStyle = MaterialTheme.typography.bodyMedium
    val speakerColor = Color(entry.speaker.colorArgb)
    Text(
        text = buildAnnotatedString {
            val nameStyle = SpanStyle(
                color = speakerColor,
                fontSize = fontSize,
                letterSpacing = fontSize * NAME_TRACKING_EM,
            )
            withStyle(nameStyle) { append(entry.speaker.name.uppercase()) }
            entry.check?.let { c ->
                withStyle(SpanStyle(color = CheckLabelColor, fontSize = fontSize)) {
                    append(" [${c.difficulty.name}: ${c.result.name}]")
                }
            }
            withStyle(nameStyle) { append(" – ") }
            withStyle(SpanStyle(color = BodyColor, fontSize = fontSize)) { append(entry.text) }
        },
        modifier = modifier.graphicsLayer { this.alpha = animatedAlpha },
        style = bodyStyle.copy(
            color = BodyColor,
            fontSize = fontSize,
            lineHeight = lineHeight,
            textIndent = TextIndent(firstLine = 0.sp, restLine = fontSize * HANGING_INDENT_EM),
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
    )
}
