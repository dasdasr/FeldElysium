package com.feldelysium.app.presentation.settings.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.presentation.theme.FONT_CUSTOM
import com.feldelysium.app.presentation.theme.SystemFontEntry
import com.feldelysium.app.presentation.theme.loadSystemFonts
import com.feldelysium.app.presentation.theme.rememberCustomFontFamily
import com.feldelysium.app.presentation.theme.rememberPlaybackFontFamily
import com.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.feldelysium.designsystem.components.container.FeldDialog
import com.feldelysium.designsystem.components.container.FeldDialogFooter
import com.feldelysium.designsystem.components.container.FeldEntryCard
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.theme.Alegreya
import com.feldelysium.designsystem.theme.FeldPalette
import java.io.File

private val SampleTexture: Int? = null

private enum class SkillColor(val color: Color) {
    Intellect(Color(0xFF5BB9C3)),
    Psyche(Color(0xFF8F6FD6)),
    Physique(Color(0xFFB02E5E)),
    Motorics(Color(0xFFCDB03A)),
}

@Immutable
private class Sample(@StringRes val speaker: Int, val color: Color, @StringRes val text: Int)

private val Samples = listOf(
    Sample(R.string.skill_electrochemistry, SkillColor.Physique.color, R.string.font_sample_01),
    Sample(R.string.skill_encyclopedia, SkillColor.Intellect.color, R.string.font_sample_02),
    Sample(R.string.skill_inland_empire, SkillColor.Psyche.color, R.string.font_sample_03),
    Sample(R.string.skill_savoir_faire, SkillColor.Motorics.color, R.string.font_sample_04),
    Sample(R.string.skill_perception, SkillColor.Motorics.color, R.string.font_sample_05),
    Sample(R.string.skill_authority, SkillColor.Psyche.color, R.string.font_sample_06),
    Sample(R.string.skill_volition, SkillColor.Psyche.color, R.string.font_sample_07),
    Sample(R.string.skill_electrochemistry, SkillColor.Physique.color, R.string.font_sample_08),
    Sample(R.string.skill_authority, SkillColor.Psyche.color, R.string.font_sample_09),
    Sample(R.string.skill_volition, SkillColor.Psyche.color, R.string.font_sample_10),
    Sample(R.string.skill_electrochemistry, SkillColor.Physique.color, R.string.font_sample_11),
    Sample(R.string.skill_rhetoric, SkillColor.Intellect.color, R.string.font_sample_12),
    Sample(R.string.skill_inland_empire, SkillColor.Psyche.color, R.string.font_sample_13),
    Sample(R.string.skill_authority, SkillColor.Psyche.color, R.string.font_sample_14),
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
        title = stringResource(R.string.settings_typeface_title),
        eyebrow = stringResource(R.string.settings_playback_section),
        subtitle = stringResource(R.string.font_dialog_subtitle),
        onDismiss = onDismiss,
        modifier = modifier,
        footer = {
            FeldDialogFooter(
                onReset = {
                    haptics.thud()
                    onReset()
                },
                onCancel = onDismiss,
                confirmLabel = stringResource(R.string.common_done),
                buttonSpec = FeldButtonDefaults.Cream,
            )
        },
    ) {
        Text(
            text = stringResource(R.string.font_dialog_hint),
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
            label = stringResource(
                if (custom == null) R.string.font_add_own else R.string.font_replace_own,
            ),
            hint = stringResource(R.string.font_file_hint),
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
            speakerName = stringResource(sample.speaker),
            speakerColor = sample.color,
            text = stringResource(sample.text),
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
            FeldIcon(
                id = FeldIconId.Accept,
                markerFilmSpec = FeldMarker.Chalk.copy(color = FeldPalette.Orange),
                size = 18.dp,
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
                text = stringResource(R.string.font_tile_glyph),
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
