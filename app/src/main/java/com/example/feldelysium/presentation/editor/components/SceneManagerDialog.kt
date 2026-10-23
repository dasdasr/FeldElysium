package com.example.feldelysium.presentation.editor.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feldelysium.designsystem.components.FeldDistressedTitle
import com.example.feldelysium.designsystem.components.FeldDistressedTitles
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.entryRough
import com.example.feldelysium.designsystem.components.display.DialogueStrip
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldMenu
import com.example.feldelysium.designsystem.components.input.FeldMenuItem
import com.example.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.SceneInfo
import kotlinx.coroutines.delay

private val FeaturedHeight = 214.dp
private val CompactHeight = 188.dp
private val AccentOrange = Color(0xFFE45720)
private val Ink = Color(0xFF1B1612)

private val CreamSheet = FeldPaper.DryButtonCream.copy(
    wear = 0.22f,
    edgeWear = 0.9f,
    edgeBand = 12.dp,
    chipCoverage = 0.5f,
    maxLoss = 0.45f,
    mottleStrength = 0.22f,
    grainStrength = 0.05f,
    speckleStrength = 0.07f,
    inkWidth = 0.9.dp,
    inkMinAlpha = 0.8f,
    inkContrast = 1.4f,
)

@Composable
private fun rememberOrangeBrush(seed: Int): Brush {
    val density = LocalDensity.current.density
    return remember(seed, density) {
        accentBrush(AccentOrange, seed, density, fade = 0.5f, glow = 0.3f, grain = 0.04f)
    }
}

@Composable
fun SceneManagerDialog(
    scenes: List<SceneInfo>,
    currentId: String,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit,
    onClearScene: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var menuId by remember { mutableStateOf<String?>(null) }
    val haptics = rememberFeldHaptics()
    val currentLines = scenes.firstOrNull { it.id == currentId }?.lines ?: 0
    val ordered = remember(scenes, currentId) { scenes.sortedBy { if (it.id == currentId) 0 else 1 } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ) {
            ManagerTopBar(count = scenes.size, onBack = onDismiss)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "title", span = { GridItemSpan(maxLineSpan) }) {
                    ManagerTitle()
                }
                itemsIndexed(
                    ordered,
                    key = { _, s -> s.id },
                    span = { _, s -> if (s.id == currentId) GridItemSpan(maxLineSpan) else GridItemSpan(1) },
                ) { index, s ->
                    val isCurrent = s.id == currentId
                    SceneFolder(
                        scene = s,
                        index = index,
                        featured = isCurrent,
                        menuOpen = menuId == s.id,
                        onClick = {
                            haptics.tick()
                            onOpen(s.id)
                        },
                        onLongClick = {
                            haptics.thud()
                            menuId = s.id
                        },
                        onDismissMenu = { menuId = null },
                        onClear = {
                            menuId = null
                            if (isCurrent) onClear() else onClearScene(s.id)
                        },
                        onDelete = {
                            menuId = null
                            onDelete(s.id)
                        },
                    )
                }
                item(key = "new-scene", span = { GridItemSpan(maxLineSpan) }) {
                    NewSceneCard(
                        onClick = {
                            haptics.tick()
                            onNew()
                        },
                    )
                }
            }

            ActionBar(
                canClear = currentLines > 0,
                onClear = onClear,
                onImport = onImport,
                onExport = onExport,
            )
        }
    }
}

@Composable
private fun ManagerTopBar(count: Int, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldIconButton(FeldIconId.Back, onBack)
        Text(
            text = "DIALOGUE EDITOR",
            style = FeldDecalType.Base.copy(fontSize = 12.sp, letterSpacing = 1.6.sp),
            color = FeldPalette.Cream,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
        Text(
            text = count.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, lineHeight = 28.sp),
            color = FeldPalette.Orange,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (count == 1) "SAVED" else "SAVED",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = FeldPalette.Muted,
        )
    }
}

@Composable
private fun ManagerTitle() {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .padding(start = 2.dp)
                .graphicsLayer {
                    rotationZ = -1.2f
                    transformOrigin = TransformOrigin(0f, 1f)
                },
        ) {
            FeldDistressedTitle(
                text = "SCENES",
                seed = 1,
                spec = FeldDistressedTitles.Large,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 64.sp,
                    lineHeight = 60.sp,
                    letterSpacing = 0.sp,
                ),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(FeldPalette.Orange),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "HOLD A FOLDER FOR MORE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                color = FeldPalette.Muted,
            )
        }
        Spacer(Modifier.height(10.dp))
        FeldDivider(seed = 7)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SceneFolder(
    scene: SceneInfo,
    index: Int,
    featured: Boolean,
    menuOpen: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
) {
    val seed = scene.id.hashCode()
    val rough = remember(seed) { entryRough(seed) }
    val orange = rememberOrangeBrush(seed + 5)

    val fg = if (featured) Ink else MaterialTheme.colorScheme.onSurface
    val sub = if (featured) Ink.copy(alpha = 0.62f) else FeldPalette.Muted
    val lineWord = if (scene.lines == 1) "LINE" else "LINES"

    val tilt = if (featured) {
        0f
    } else if (index % 2 == 0) {
        -0.5f
    } else {
        0.6f
    }
    val dropY = if (!featured && index % 2 == 0) 8.dp else 0.dp

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lift by animateFloatAsState(if (pressed) 1f else 0f, tween(90), label = "lift")

    Box(
        Modifier
            .offset(y = dropY)
            .graphicsLayer {
                rotationZ = tilt
                val s = 1f - 0.025f * lift
                scaleX = s
                scaleY = s
            }
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        FeldCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (featured) FeaturedHeight else CompactHeight),
            seed = seed,
            spec = if (featured) CreamSheet else FeldPaper.Entry,
            rough = rough,
            contentPadding = PaddingValues(
                start = if (featured) 46.dp else 14.dp,
                end = 14.dp,
                top = 14.dp,
                bottom = 0.dp,
            ),
            showDecals = false,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds(),
            ) {
                Text(
                    text = scene.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (featured) 46.sp else 26.sp,
                        lineHeight = if (featured) 44.sp else 26.sp,
                        letterSpacing = 0.5.sp,
                    ),
                    color = fg,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${scene.lines} $lineWord",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = sub,
                        modifier = Modifier.weight(1f),
                    )
                    if (featured) {
                        Text(
                            text = "HOLD FOR MORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = sub.copy(alpha = 0.5f),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                if (scene.preview.isBlank()) {
                    Text(
                        text = "NOTHING ON TAPE YET",
                        style = MaterialTheme.typography.labelSmall,
                        color = sub.copy(alpha = 0.7f),
                    )
                } else {
                    Row {
                        Text(
                            text = "“",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = if (featured) 54.sp else 38.sp,
                                lineHeight = if (featured) 40.sp else 30.sp,
                            ),
                            color = FeldPalette.Orange,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            text = scene.preview.trim(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = fg.copy(alpha = 0.82f),
                            maxLines = if (featured) 3 else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (featured) 26.dp else 18.dp)
                    .markerFilmMaterial(FeldMarker.Ink.copy(color = Color.Unspecified), seed, bake = false),
            ) {
                DialogueStrip(
                    colors = scene.barcode,
                    lengths = scene.lengths,
                    seed = seed,
                    lineColor = fg,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        val ghost = if (featured) 130.sp else 88.sp
        Text(
            text = scene.lines.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = ghost, lineHeight = ghost * 0.85f),
            color = fg.copy(alpha = 0.06f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = if (featured) 40.dp else 32.dp),
        )

        if (featured) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-6).dp)
                    .width(32.dp)
                    .fillMaxHeight(0.8f)
                    .drawBehind { drawRect(orange) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "CURRENT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = Ink,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .graphicsLayer { rotationZ = -90f },
                )
            }
        }

        Box(Modifier.align(Alignment.Center)) {
            FeldMenu(
                expanded = menuOpen,
                onDismiss = onDismissMenu,
                seed = seed,
                offset = DpOffset(0.dp, 0.dp),
                items = buildList {
                    if (scene.lines > 0) add(FeldMenuItem("Clear lines", FeldIconId.Close, onClick = onClear))
                    add(FeldMenuItem("Delete", FeldIconId.Delete, destructive = true, onClick = onDelete))
                },
            )
        }
    }
}

@Composable
private fun NewSceneCard(onClick: () -> Unit) {
    val dash = FeldPalette.Muted
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind {
                val inset = 1.dp.toPx()
                drawRoundRect(
                    color = dash,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 7.dp.toPx())),
                    ),
                )
            }
            .feldClickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 44.sp),
            color = FeldPalette.Cream,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = "NEW SCENE",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
            color = FeldPalette.Muted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "TAP TO CREATE",
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun ActionBar(canClear: Boolean, onClear: () -> Unit, onImport: () -> Unit, onExport: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        FeldDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClearControl(enabled = canClear, onConfirm = onClear)
            Box(
                Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(FeldPalette.Muted.copy(alpha = 0.4f)),
            )
            GhostAction("Import", FeldIconId.Import, onImport, Modifier.weight(1f), seed = 31)
            FilmAction("Export", FeldIconId.Export, onExport, Modifier.weight(1f), seed = 8)
        }
    }
}

@Composable
private fun GhostAction(text: String, icon: FeldIconId, onClick: () -> Unit, modifier: Modifier, seed: Int) {
    val haptics = rememberFeldHaptics()
    val rough = remember(seed) { entryRough(seed) }
    FeldCard(
        modifier = modifier,
        seed = seed,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(horizontal = 10.dp),
        showDecals = false,
        onClick = {
            haptics.tick()
            onClick()
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            FeldIcon(icon, size = 20.dp, markerFilmSpec = FeldMarker.Chalk)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.markerFilmMaterial(FeldMarker.Chalk, seed + 1)) {
                Text(
                    text = text.uppercase(),
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

@Composable
private fun FilmAction(text: String, icon: FeldIconId, onClick: () -> Unit, modifier: Modifier, seed: Int) {
    val haptics = rememberFeldHaptics()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(if (pressed) 75 else 150), label = "press")
    val density = LocalDensity.current.density
    val body = remember(seed, density) {
        accentBrush(AccentOrange, seed, density, fade = 0.5f, glow = 0.3f, grain = 0.04f)
    }

    Box(
        modifier
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
            FeldIcon(icon, size = 18.dp, markerFilmSpec = FeldMarker.Ink)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.markerFilmMaterial(FeldMarker.Ink, seed + 1)) {
                Text(
                    text = text.uppercase(),
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

@Composable
private fun ClearControl(enabled: Boolean, onConfirm: () -> Unit) {
    val haptics = rememberFeldHaptics()
    var armed by remember { mutableStateOf(false) }
    val orange = rememberOrangeBrush(21)

    LaunchedEffect(armed) {
        if (armed) {
            delay(2500)
            armed = false
        }
    }
    LaunchedEffect(enabled) { if (!enabled) armed = false }

    Box(
        Modifier
            .animateContentSize()
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        if (armed) {
            Box(
                Modifier
                    .graphicsLayer { rotationZ = -6f }
                    .drawBehind { drawRect(orange) }
                    .feldClickable {
                        haptics.thud()
                        armed = false
                        onConfirm()
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "SURE?",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp, letterSpacing = 1.6.sp),
                    color = Ink,
                )
            }
        } else {
            Box(
                Modifier
                    .size(48.dp)
                    .feldClickable(enabled = enabled) {
                        haptics.tick()
                        armed = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                FeldIcon(FeldIconId.Delete, size = 22.dp)
            }
        }
    }
}
