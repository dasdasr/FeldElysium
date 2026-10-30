package com.feldelysium.app.presentation.playback.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.feldelysium.app.R
import com.feldelysium.designsystem.theme.FeldContinueStyle
import com.feldelysium.app.presentation.playback.Prompt
import kotlin.math.max
import kotlin.math.min

private val OptionOrange = Color(0xFFD9482B)
private val ContinueOrange = Color(0xFFB5361D)

@Composable
fun PlaybackPromptFooter(prompt: Prompt, metrics: FooterMetrics, onContinue: () -> Unit, onChoice: () -> Unit) {
    when (prompt) {
        is Prompt.Choices -> FadeIn { ReplyOptions(prompt, metrics, onChoice) }
        is Prompt.Continue -> FadeIn { ContinueButton(prompt.nextSpeakerColor, metrics, onContinue) }
        else -> Unit
    }
}

@Composable
private fun FadeIn(content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(PlaybackMotion.TEXT_FADE_IN_MS), label = "footerFade")
    Box(Modifier.graphicsLayer { alpha = a }) { content() }
}

@Composable
private fun ReplyOptions(p: Prompt.Choices, m: FooterMetrics, onChoice: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        p.options.forEachIndexed { i, option ->
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                        append("${i + 1}.- ")
                    }
                    withStyle(SpanStyle(color = if (p.selected && i == p.chosenIndex) Color.White else OptionOrange)) {
                        append(option)
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = m.fontSize,
                    lineHeight = m.lineHeight,
                    textIndent = TextIndent(0.sp, m.fontSize * (14f / 12f)),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        LineHeightStyle.Alignment.Proportional,
                        LineHeightStyle.Trim.None,
                    ),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interaction, indication = null) { if (!p.selected) onChoice() },
            )
        }
    }
}

private val ContinueJitter = listOf(0f, -0.02f, 0.02f, -0.015f, 0.02f, -0.02f, 0.01f, -0.01f)
private val ContinueSpacing = listOf(0.07f, 0.10f, 0.06f, 0.09f, 0.07f, 0.10f, 0.08f, 0.00f)

@Composable
private fun ContinueButton(speakerColor: Long, m: FooterMetrics, onClick: () -> Unit) {
    val color = Color(speakerColor)
    val tinted = !color.isNeutral()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(m.buttonHeight)
            .background(ContinueOrange)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (tinted) {
            Image(
                painter = painterResource(R.drawable.continue_mask),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(color, BlendMode.SrcIn),
                modifier = Modifier.matchParentSize(),
            )
        }
        val arrowSize = m.fontSize * 0.9f
        Text(
            text = buildAnnotatedString {
                "CONTINUE".forEachIndexed { i, ch ->
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift(ContinueJitter[i]),
                            letterSpacing = ContinueSpacing[i].em,
                        ),
                    ) { append(ch) }
                }
                append("  ")
                appendInlineContent("arrow", "▶")
            },
            inlineContent = mapOf(
                "arrow" to InlineTextContent(
                    Placeholder(arrowSize, arrowSize, PlaceholderVerticalAlign.TextCenter),
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            ),
            color = Color.White,
            style = FeldContinueStyle.copy(fontSize = m.fontSize * 1.1f),
            modifier = Modifier.padding(start = 12.dp).offset(y = 1.dp),
        )
    }
}

internal fun Color.isNeutral(): Boolean {
    val mx = max(red, max(green, blue))
    val mn = min(red, min(green, blue))
    return mx == 0f || (mx - mn) / mx < 0.15f
}
