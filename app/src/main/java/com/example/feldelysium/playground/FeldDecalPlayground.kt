package com.example.feldelysium.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldEntryCard
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.components.input.FeldChipFill
import com.example.feldelysium.designsystem.components.input.FeldChipShape
import com.example.feldelysium.designsystem.components.input.FeldSlider
import com.example.feldelysium.designsystem.components.input.FeldSliderColors
import com.example.feldelysium.designsystem.components.input.FeldSwitch
import com.example.feldelysium.designsystem.components.input.FeldThumb
import com.example.feldelysium.designsystem.foundation.decal.DecalKind
import com.example.feldelysium.designsystem.foundation.decal.DecalSpec
import com.example.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.theme.DiscoDialogueTheme

private val Bg = Color(0xFF0B1011)

@Composable
fun FeldDecalPlayground(modifier: Modifier = Modifier) {
    var ambient by remember { mutableFloatStateOf(0.5f) }
    var seed by remember { mutableIntStateOf(1) }
    var preset by remember { mutableStateOf("Entry") }
    var enabled by remember { mutableStateOf(DecalKind.values().toSet() - DecalKind.Sprite) }
    var maxDecals by remember { mutableFloatStateOf(4f) }
    var leak by remember { mutableFloatStateOf(0.25f) }
    var dragging by remember { mutableStateOf(false) }
    var bake by remember { mutableStateOf(true) }
    var stress by remember { mutableStateOf(false) }

    val base = when (preset) {
        "Dialog" -> DecalSpec.Dialog; "Panel" -> DecalSpec.Panel; else -> DecalSpec.Entry
    }
    val spec = base.copy(
        weights = enabled.associateWith { base.weights[it] ?: 1f },
        maxDecals = maxDecals.toInt(),
        leakChance = leak,
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Title("CHIPS") }
        item {
            var sel by remember { mutableStateOf(setOf("Logic")) }
            var shape by remember { mutableStateOf(FeldChipShape.Chamfer) }
            var fillShape by remember { mutableStateOf(FeldChipFill.Diamond) }
            var outline by remember { mutableStateOf(true) }
            var leading by remember { mutableStateOf(true) }
            val names = listOf("Logic", "Empathy", "Drama", "Volition")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeldChipShape.values().forEach {
                        FeldChip(
                            text = it.name,
                            selected = shape == it,
                            onClick = { shape = it },
                            shape = it,
                            leading = true,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FeldSwitch(outline, { outline = it }); Spacer(Modifier.width(8.dp)); Text("outline", color = Color.White)
                    Spacer(Modifier.width(16.dp))
                    FeldSwitch(leading, { leading = it }); Spacer(Modifier.width(8.dp)); Text("diamond", color = Color.White)
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    names.forEachIndexed { i, n ->
                        FeldChip(
                            text = n, selected = n in sel,
                            onClick = { sel = if (n in sel) sel - n else sel + n },
                            shape = shape, outline = outline, leading = leading,
                            fillShape = FeldChipFill.Diamond,
                            accent = listOf(FeldChipColors.Teal, FeldChipColors.Orange, FeldChipColors.Cream, Color(0xFF8F6FD6))[i],
                            seed = 100 + i,
                        )
                    }
                    FeldChip("Disabled", selected = false, onClick = {}, enabled = false, shape = shape, outline = outline, leading = leading)
                }
            }
        }
        item { Title("DECALS") }
        item {
            Column {
                Column {
                    FeldSlider(
                        value = ambient,
                        onValueChange = { ambient = it },
                        label = "ambient",
                        valueText = { "%.2f".format(it) },
                        thumb = FeldThumb.Rectangle,
                    )

                    FeldSlider(
                        value = maxDecals,
                        onValueChange = { maxDecals = it },
                        valueRange = 0f..8f,
                        label = "max decals",
                        valueText = { it.toInt().toString() },
                        thumb = FeldThumb.Circle,
                        accent = FeldSliderColors.Teal,
                    )

                    FeldSlider(
                        value = leak,
                        onValueChange = { leak = it },
                        label = "leak chance",
                        valueText = { "%.2f".format(it) },
                        thumb = FeldThumb.Diamond,
                        accent = FeldSliderColors.Orange,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton({ seed-- }) { Text("seed −") }
                    Text("seed $seed", color = Color.White)
                    OutlinedButton({ seed++ }) { Text("seed +") }
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Entry", "Dialog", "Panel").forEach {
                        FeldChip(
                            text = it,
                            selected = preset == it,
                            onClick = { preset = it },
                        )
                    }
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DecalKind.values().forEach { k ->
                        FeldChip(
                            text = k.name,
                            selected = k in enabled,
                            onClick = {
                                enabled = if (k in enabled) enabled - k else enabled + k
                            },
                        )
                    }
                }
            }
        }

        item { Title("ENTRY CARDS") }
        itemsIndexed(
            listOf(
                Triple("Logic", Color(0xFF4FB3A6), "This is impossible. He is not from Revachol."),
                Triple("Empathy", Color(0xFFD06A8B), "He looks lonely."),
                Triple(
                    "Drama",
                    Color(0xFF8F6FD6),
                    "Oh, what a magnificent entrance. The orange clothes."
                ),
            )
        ) { i, (n, c, t) ->
            FeldEntryCard(
                n, c, t, number = i + 1, seed = seed + i, ambient = ambient,
                decalSpec = spec, isDragging = dragging && i == 2, onMore = {},
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeldSwitch(dragging, { dragging = it }); Spacer(Modifier.width(8.dp))
                Text("drag state (heartbeat diamond)", color = Color.White)
            }
        }

        item { Title("PANEL / DIALOG-SIZE") }
        item {
            FeldCard(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp), seed = seed + 10,
                ambient = ambient, decalSpec = spec
            ) { Text("PANEL 200dp", color = FeldPalette.Muted) }
        }
        item {
            FeldCard(
                Modifier
                    .fillMaxWidth()
                    .height(380.dp), seed = seed + 11,
                ambient = ambient, decalSpec = spec
            ) { Text("DIALOG 380dp", color = FeldPalette.Muted) }
        }

        item { Title("MARKER FILM") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeldSwitch(bake, { bake = it }); Spacer(Modifier.width(8.dp))
                Text("bake to bitmap", color = Color.White)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "Chalk" to FeldMarker.Chalk,
                    "Ink" to FeldMarker.Ink,
                    "Dry" to FeldMarker.Dry
                ).forEachIndexed { i, (name, s) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            name.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier
                                .markerFilmMaterial(
                                    s,
                                    seed + i,
                                    bakeKey = name,
                                    bake = bake
                                )
                                .padding(8.dp)
                        )
                        FeldIconId.values().take(6).forEach {
                            FeldIcon(
                                it, tint = Color.White, size = 28.dp,
                                modifier = Modifier.markerFilmMaterial(
                                    s,
                                    seed + it.ordinal,
                                    bakeKey = name,
                                    bake = bake
                                )
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeldSwitch(stress, { stress = it }); Spacer(Modifier.width(8.dp))
                Text("stress: 80 film icons (check scroll)", color = Color.White)
            }
        }
        if (stress) item {
            Box(Modifier.height(240.dp)) {
                LazyVerticalGrid(GridCells.Fixed(8), userScrollEnabled = true) {
                    items(80) { i ->
                        val id = FeldIconId.values()[i % FeldIconId.values().size]
                        FeldIcon(
                            id, tint = Color.White, size = 28.dp,
                            modifier = Modifier
                                .padding(6.dp)
                                .markerFilmMaterial(FeldMarker.Chalk, i, bakeKey = id, bake = bake)
                        )
                    }
                }
            }
        }

        item { Title("DECAL FONTS") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "REEL NO. 4/SIDE B/SEGMENT 0023-0045",
                    style = FeldDecalType.Base.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = FeldPalette.Muted
                )
                Text(
                    "TECHNICAL REVIEW: OK - J.M. 10/26/83",
                    style = FeldDecalType.Base.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FeldPalette.Muted
                )
                Text(
                    "FELD • PLAYBACK • DEVICE",
                    style = FeldDecalType.Label.copy(fontSize = 16.sp),
                    color = FeldPalette.Cream
                )
                Text(
                    "REC. 0024-B   73   74",
                    style = FeldDecalType.Label.copy(fontSize = 22.sp),
                    color = FeldPalette.Orange
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun Title(t: String) =
    Text(
        t,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
        color = FeldPalette.Muted
    )

@Composable
private fun LabeledSlider(
    label: String,
    v: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        Slider(v, onChange, valueRange = range)
    }
}

@Preview(widthDp = 380, heightDp = 900, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun DecalPlaygroundPreview() = DiscoDialogueTheme { FeldDecalPlayground() }
