package com.example.feldelysium.presentation.settings.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.example.feldelysium.designsystem.components.card.FeldEntryCard
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.theme.Alegreya
import com.example.feldelysium.theme.FONT_CUSTOM
import com.example.feldelysium.theme.SystemFontEntry
import com.example.feldelysium.theme.loadSystemFonts
import com.example.feldelysium.theme.rememberCustomFontFamily
import com.example.feldelysium.theme.rememberPlaybackFontFamily
import java.io.File

private val SampleTexture: Int? = null

@Immutable
private class Sample(val speaker: String, val color: Color, val text: String)

private enum class SkillColor(val color: Color) {
    Intellect(Color(0xFF5BB9C3)),
    Psyche(Color(0xFF8F6FD6)),
    Physique(Color(0xFFB02E5E)),
    Motorics(Color(0xFFCDB03A)),
}

private val Samples = listOf(
    Sample(
        "Electrochemistry",
        SkillColor.Physique.color,
        "Oh, this one f*cks. Look at those curves. Look at that filthy little lowercase g."
    ),
    Sample("Encyclopedia", SkillColor.Intellect.color, "Historically, the serif emerged as a response to—"),
    Sample("Inland Empire", SkillColor.Psyche.color, "Close your eyes. Listen to the letters."),
    Sample("Savoir Faire", SkillColor.Motorics.color, "Go with the ugly one. Make it look intentional."),
    Sample("Perception", SkillColor.Motorics.color, "Look at that lowercase ‘a’. Something is deeply wrong with it."),
    Sample(
        "Authority",
        SkillColor.Psyche.color,
        "You will choose the serif. You will respect the serif. You will not ask."
    ),
    Sample("Volition", SkillColor.Psyche.color, "We are making an interface, not a suicide note."),
    Sample("Electrochemistry", SkillColor.Physique.color, "The bold one. The f*cking bold one."),
    Sample("Authority", SkillColor.Psyche.color, "Nobody respects a man whose letters look cheap."),
    Sample("Volition", SkillColor.Psyche.color, "Choose something readable."),
    Sample("Electrochemistry", SkillColor.Physique.color, "Choose something unreasonably sexy."),
    Sample(
        "Rhetoric",
        SkillColor.Intellect.color,
        "Sans-serif is a bourgeois conspiracy to make everything look like a startup."
    ),
    Sample(
        "Inland Empire",
        SkillColor.Psyche.color,
        "This one smells like wet concrete and an inspiration you can no longer access."
    ),
    Sample("Authority", SkillColor.Psyche.color, "Commit. To. The. Font. Detective."),
)

@Stable
private class FontTile(val key: String, val label: String, make: () -> FontFamily) {
    val family: FontFamily by lazy(make)
}

@Composable
fun PlaybackFontDialog(
    settings: AppSettings,
    onSelect: (String) -> Unit,
    onReset: () -> Unit,
    onUpload: (Uri) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberFeldHaptics()
    val family = rememberPlaybackFontFamily(settings)
    val custom = rememberCustomFontFamily(settings.customFontFile)
    val system by produceState(emptyList<SystemFontEntry>()) { value = loadSystemFonts() }

    var firstIndex by rememberSaveable { mutableIntStateOf(0) }
    var secondIndex by rememberSaveable { mutableIntStateOf(1) }

    val tiles = remember(custom, settings.customFontName, system) {
        buildList {
            add(FontTile("", "Alegreya") { Alegreya })
            if (custom != null) add(FontTile(FONT_CUSTOM, settings.customFontName) { custom })
            system.forEach { add(FontTile(it.path, it.name) { FontFamily(Font(File(it.path))) }) }
        }
    }
    val selectedKey = remember(tiles, settings.fontChoice) {
        tiles.firstOrNull { it.key == settings.fontChoice }?.key ?: ""
    }

    val listState = rememberLazyListState()
    LaunchedEffect(tiles.size) {
        val i = tiles.indexOfFirst { it.key == selectedKey }
        if (i > 0) listState.scrollToItem(i)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onUpload)
    }

    FeldDialog(
        title = "Typeface",
        eyebrow = "Playback",
        subtitle = "The right font changes the whole mood.",
        onDismiss = onDismiss,
        modifier = modifier,
        footer = {
            FeldDialogFooter(
                onReset = {
                    haptics.thud()
                    onReset()
                },
                onCancel = onDismiss,
                confirmLabel = "Done",
                buttonSpec = FeldButtonDefaults.Cream,
            )
        },
    ) {
        Text(
            text = "TAP A CARD TO CHANGE THE LINES  › › ›",
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
        )
        Spacer(Modifier.height(12.dp))

        SampleStack(
            firstIndex = firstIndex,
            secondIndex = secondIndex,
            family = family,
            onFirstClick = {
                haptics.tick()
                firstIndex = (firstIndex + 1) % Samples.size
            },
            onSecondClick = {
                haptics.tick()
                secondIndex = (secondIndex + 1) % Samples.size
            },
        )
        Spacer(Modifier.height(18.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(tiles, key = { it.key }) { tile ->
                FontTileCard(
                    tile = tile,
                    selected = tile.key == selectedKey,
                    onClick = {
                        haptics.tick()
                        onSelect(tile.key)
                    },
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        DashedAddButton(
            label = if (custom == null) "ADD YOUR OWN FONT" else "REPLACE YOUR FONT",
            hint = ".ttf or .otf",
            onClick = {
                haptics.tick()
                picker.launch(arrayOf("*/*"))
            },
        )
    }
}

@Composable
private fun SampleStack(
    firstIndex: Int,
    secondIndex: Int,
    family: FontFamily,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        SampleTexture?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.5f),
            )
        }

        Column(Modifier.padding(horizontal = 6.dp, vertical = 12.dp)) {
            SampleCard(
                sample = Samples[firstIndex],
                family = family,
                seed = 5,
                number = 1,
                tilt = -1.6f,
                onClick = onFirstClick,
                modifier = Modifier.padding(end = 18.dp),
            )

            Spacer(Modifier.height(10.dp))

            SampleCard(
                sample = Samples[secondIndex],
                family = family,
                seed = 9,
                number = 2,
                tilt = 1.4f,
                onClick = onSecondClick,
                modifier = Modifier.padding(start = 18.dp),
            )
        }
    }
}

@Composable
private fun SampleCard(
    sample: Sample,
    family: FontFamily,
    seed: Int,
    number: Int,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.graphicsLayer { rotationZ = tilt }) {
        FeldEntryCard(
            speakerName = sample.speaker,
            speakerColor = sample.color,
            text = sample.text,
            number = number,
            ambient = 0.7f,
            seed = seed,
            fontFamily = family,
            onClick = onClick,
        )
    }
}

@Composable
private fun FontTileCard(tile: FontTile, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier
            .width(92.dp)
            .height(100.dp)
            .background(Color.White.copy(alpha = 0.04f), shape)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) FeldPalette.Orange else Color.White.copy(alpha = 0.12f),
                shape = shape,
            )
            .feldClickable(onClick = onClick),
    ) {
        if (selected) {
            Text(
                text = "✓",
                color = FeldPalette.Orange,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp),
            )
            FeldIcon(
                id = FeldIconId.Accept,
                markerFilmSpec = FeldMarker.Chalk.copy(color = FeldPalette.Orange),
                size = 16.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp),
            )
        }
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Aa",
                style = TextStyle(fontFamily = tile.family, fontSize = 34.sp),
                color = FeldPalette.Cream,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tile.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) FeldPalette.Orange else FeldPalette.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DashedAddButton(label: String, hint: String, onClick: () -> Unit) {
    val color = FeldPalette.Muted
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = color,
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 7.dp.toPx())),
                    ),
                )
            }
            .feldClickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldIcon(FeldIconId.Import)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = FeldPalette.Muted)
        }
    }
}
