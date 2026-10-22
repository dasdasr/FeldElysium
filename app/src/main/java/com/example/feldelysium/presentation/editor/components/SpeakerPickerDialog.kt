package com.example.feldelysium.presentation.editor.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feldelysium.designsystem.components.button.FeldTextButton
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.card.entryRough
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.settings.FeldSectionHeader
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.model.groupedByTag
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private data class SpeakerGroup(val type: SpeakerType, val title: String, val items: List<Speaker>)

private const val CHIP_VARIANTS = 4
private const val CHIP_SEED_BASE = 100
private val ChipHeight = 44.dp

private val FadeStops = listOf(
    0f to 1f,
    0.2f to 0.9f,
    0.4f to 0.65f,
    0.6f to 0.35f,
    0.8f to 0.12f,
    1f to 0f,
)

@Preview
@Composable
private fun SpeakerPickerDialogPreview() {
    SpeakerPickerDialog(
        speakers = PredefinedSpeakers.ALL + Speaker(
            id = "custom",
            name = "Ancient Reptilian Brain",
            colorArgb = Speaker.DEFAULT_CUSTOM_COLOR,
            type = SpeakerType.CUSTOM,
        ),
        onSpeakerSelected = {},
        onCreateCustom = {},
        onEditCustom = {},
        onDismiss = {},
        tags = listOf(SpeakerType.NPC),
    )
}

@Composable
fun SpeakerPickerDialog(
    speakers: List<Speaker>,
    tags: List<SpeakerType>,
    onSpeakerSelected: (Speaker) -> Unit,
    onCreateCustom: () -> Unit,
    onEditCustom: (Speaker) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var collapsed by remember { mutableStateOf(setOf<SpeakerType>()) }

    val q = query.trim()
    val roughs = remember { List(CHIP_VARIANTS) { entryRough(CHIP_SEED_BASE + it) } }

    val groups = remember(speakers, tags, q) {
        speakers
            .filter { q.isEmpty() || it.name.contains(q, ignoreCase = true) }
            .groupedByTag(tags)
            .map { (tag, items) ->
                SpeakerGroup(
                    type = tag,
                    title = if (tag.isCustom) "Custom" else tag.name,
                    items = items,
                )
            }
            .filter { it.items.isNotEmpty() }
            .sortedBy { if (it.type == SpeakerType.YOU) 0 else 1 }
    }

    val bg = MaterialTheme.colorScheme.background

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .systemBarsPadding()
                .imePadding(),
        ) {
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Dialogue line",
                navIcon = FeldIconId.Close,
                onNav = onDismiss,
                divider = false,
                titleStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    letterSpacing = 1.sp,
                ),
            )

            Spacer(Modifier.height(8.dp))
            SearchField(value = query, onValueChange = { query = it })

            Box(Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp, bottom = 16.dp),
                ) {
                    if (groups.isEmpty()) {
                        Text(
                            text = "NO SPEAKERS FOUND",
                            style = MaterialTheme.typography.labelMedium,
                            color = FeldPalette.Muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                        )
                    }

                    groups.forEachIndexed { gi, group ->
                        key(group.type) {
                            val expanded = q.isNotEmpty() || group.type !in collapsed

                            Spacer(Modifier.height(if (gi == 0) 0.dp else 10.dp))
                            FeldSectionHeader(
                                title = group.title,
                                seed = 40 + gi,
                                modifier = Modifier.alpha(if (expanded) 1f else 0.6f),
                                onClick = {
                                    collapsed = if (group.type in collapsed) {
                                        collapsed - group.type
                                    } else {
                                        collapsed + group.type
                                    }
                                },
                                trailing = {
                                    Text(
                                        text = group.items.size.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FeldPalette.Muted,
                                    )
                                },
                            )

                            if (expanded) {
                                group.items.chunked(2).forEachIndexed { ri, pair ->
                                    SpeakerRow(
                                        pair = pair,
                                        rowIndex = ri,
                                        roughs = roughs,
                                        top = if (ri == 0) 18.dp else 6.dp,
                                        onSelect = onSpeakerSelected,
                                        onEdit = onEditCustom,
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Brush.verticalGradient(colorStops = FadeStops.map { (t, a) -> t to bg.copy(alpha = a) }
                            .toTypedArray())),
                )
            }

            FeldDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FilmButton(text = "CREATE CUSTOM SPEAKER", onClick = onCreateCustom)
                FeldTextButton("Cancel", onDismiss)
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    val rough = remember { entryRough(77) }
    FeldCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        seed = 77,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(horizontal = 14.dp),
        showDecals = false,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeldIcon(FeldIconId.Search, size = 19.dp)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(FeldPalette.Orange),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "SEARCH",
                                style = MaterialTheme.typography.labelMedium,
                                color = FeldPalette.Muted.copy(alpha = 0.7f),
                            )
                        }
                        inner()
                    }
                },
            )
            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .feldClickable(onClick = { onValueChange("") }),
                    contentAlignment = Alignment.Center,
                ) {
                    FeldIcon(FeldIconId.Close, size = 13.dp)
                }
            }
        }
    }
}

@Composable
private fun SpeakerRow(
    pair: List<Speaker>,
    rowIndex: Int,
    roughs: List<RoughStyle>,
    top: Dp,
    onSelect: (Speaker) -> Unit,
    onEdit: (Speaker) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = top),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        pair.forEachIndexed { ci, speaker ->
            val variant = (rowIndex * 2 + ci) % CHIP_VARIANTS
            SpeakerChip(
                speaker = speaker,
                seed = CHIP_SEED_BASE + variant,
                rough = roughs[variant],
                onClick = { onSelect(speaker) },
                onEdit = if (!speaker.isBundled) {
                    { onEdit(speaker) }
                } else {
                    null
                },
            )
        }
        if (pair.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RowScope.SpeakerChip(
    speaker: Speaker,
    seed: Int,
    rough: RoughStyle,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
) {
    val color = Color(speaker.colorArgb)
    FeldCard(
        modifier = Modifier.weight(1f),
        seed = seed,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(start = 12.dp, end = if (onEdit != null) 2.dp else 10.dp),
        showDecals = false,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChipHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeldHandDiamond(color, speaker.id.hashCode(), 16.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = speaker.name.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (onEdit != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .feldClickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    FeldIcon(FeldIconId.Edit, size = 16.dp)
                }
            }
        }
    }
}

/** Wide version of FeldFab.Film: orange accent body with perforation, ink-film icon and text. */
@Composable
private fun FilmButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, seed: Int = 7) {
    val haptics = rememberFeldHaptics()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(if (pressed) 75 else 150),
        label = "press",
    )
    val density = LocalDensity.current.density
    val body = remember(seed, density) {
        accentBrush(Color(0xFFE45720), seed, density, fade = 0.5f, glow = 0.3f, grain = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                val s = 1f - 0.02f * press
                scaleX = s
                scaleY = s
                translationY = 1.5f * press
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(4.dp),
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.tick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            drawRoundRect(brush = body, cornerRadius = CornerRadius(4.dp.toPx()))

            val pitch = 10.dp.toPx()
            val hw = 5.dp.toPx()
            val hh = 3.5.dp.toPx()
            val inset = 3.dp.toPx()
            val edge = 3.dp.toPx()
            val fade = pitch * 0.6f
            val shift = pitch * 0.5f * press
            val count = (size.width / pitch).toInt() + 2

            for (k in -1..count) {
                val x = 6.dp.toPx() + k * pitch - shift
                val a = ((x - edge) / fade).coerceIn(0f, 1f) *
                        ((size.width - edge - (x + hw)) / fade).coerceIn(0f, 1f)
                if (a <= 0f) continue
                val hole = Color.Black.copy(alpha = a)
                val r = CornerRadius(1.dp.toPx())
                drawRoundRect(hole, Offset(x, inset), Size(hw, hh), r, blendMode = BlendMode.DstOut)
                drawRoundRect(
                    hole,
                    Offset(x, size.height - inset - hh),
                    Size(hw, hh),
                    r,
                    blendMode = BlendMode.DstOut,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            FeldIcon(
                FeldIconId.Add,
                size = 18.dp,
                markerFilmSpec = FeldMarker.Ink,
                modifier = Modifier.padding(bottom = 2.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.markerFilmMaterial(FeldMarker.Ink, seed + 1)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}
