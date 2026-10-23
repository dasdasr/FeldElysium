package com.example.feldelysium.presentation.editor.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feldelysium.designsystem.components.button.FeldTextButton
import com.example.feldelysium.designsystem.components.card.FeldCard
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

private data class SpeakerGroup(val type: SpeakerType, val title: String, val items: List<Speaker>)

private sealed interface PickerItem {
    val key: String

    data class Header(val group: SpeakerGroup, val index: Int) : PickerItem {
        override val key get() = "h_${group.type.id}"
    }

    data class Pair(val speakers: List<Speaker>, val row: Int, val groupId: String) : PickerItem {
        override val key get() = "r_${groupId}_$row"
    }
}

private const val CHIP_VARIANTS = 4
private const val CHIP_SEED_BASE = 100
private val ChipHeight = 44.dp
private val AvatarWidth = 40.dp
private val AvatarHeight = 56.dp

private val FadeStops = listOf(
    0f to 1f,
    0.2f to 0.9f,
    0.4f to 0.65f,
    0.6f to 0.35f,
    0.8f to 0.12f,
    1f to 0f,
)

private val avatarCache = LruCache<String, ImageBitmap>(64)

private fun decodeAvatar(uri: String, targetPx: Int): ImageBitmap? {
    avatarCache.get(uri)?.let { return it }
    val path = Uri.parse(uri).path ?: return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= targetPx && bounds.outHeight / (sample * 2) >= targetPx) sample *= 2
    val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return null
    return bmp.asImageBitmap().also { avatarCache.put(uri, it) }
}

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
    var avatarMode by remember { mutableStateOf(true) }

    var q by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        val t = query.trim()
        if (t.isNotEmpty() && q.isNotEmpty()) delay(120)
        q = t
    }
    val roughs = remember { List(CHIP_VARIANTS) { entryRough(CHIP_SEED_BASE + it) } }
    val seen = remember { HashSet<String>() }
    var settled by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        withFrameNanos { }
        settled = true
    }
    val density = LocalDensity.current
    LaunchedEffect(speakers) {
        val px = with(density) { AvatarHeight.roundToPx() }
        withContext(Dispatchers.IO) {
            speakers.mapNotNull { it.customIconUri }.take(16).forEach { decodeAvatar(it, px) }
        }
    }

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

    val items = remember(groups, collapsed, q) {
        buildList {
            groups.forEachIndexed { gi, group ->
                add(PickerItem.Header(group, gi))
                if (q.isNotEmpty() || group.type !in collapsed) {
                    group.items.chunked(2).forEachIndexed { ri, pair ->
                        add(PickerItem.Pair(pair, ri, group.type.id))
                    }
                }
            }
        }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                )
                ViewToggle(avatarMode = avatarMode, onClick = { avatarMode = !avatarMode })
            }

            Box(Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                ) {
                    if (items.isEmpty()) {
                        item(key = "empty") {
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
                    }

                    itemsIndexed(items, key = { _, it -> it.key }) { index, item ->
                        Box(Modifier.entrance(item.key, seen, index, animate = settled)) {
                            when (item) {
                                is PickerItem.Header -> {
                                    val expanded = q.isNotEmpty() || item.group.type !in collapsed
                                    Column {
                                        Spacer(Modifier.height(if (item.index == 0) 0.dp else 10.dp))
                                        FeldSectionHeader(
                                            title = item.group.title,
                                            seed = 40 + item.index,
                                            modifier = Modifier.alpha(if (expanded) 1f else 0.6f),
                                            onClick = {
                                                val t = item.group.type
                                                collapsed = if (t in collapsed) collapsed - t else collapsed + t
                                            },
                                            trailing = {
                                                Text(
                                                    text = item.group.items.size.toString(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = FeldPalette.Muted,
                                                )
                                            },
                                        )
                                    }
                                }

                                is PickerItem.Pair -> SpeakerRow(
                                    pair = item.speakers,
                                    rowIndex = item.row,
                                    roughs = roughs,
                                    top = when {
                                        avatarMode -> if (item.row == 0) 8.dp else 2.dp
                                        else -> if (item.row == 0) 8.dp else 0.dp
                                    },
                                    avatarMode = avatarMode,
                                    onSelect = onSpeakerSelected,
                                    onEdit = onEditCustom,
                                )
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(
                            Brush.verticalGradient(
                                colorStops = FadeStops.map { (t, a) -> t to bg.copy(alpha = a) }
                                    .toTypedArray(),
                            ),
                        ),
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
private fun Modifier.entrance(key: String, seen: MutableSet<String>, index: Int, animate: Boolean): Modifier {
    val instant = remember(key) { !animate || key in seen }
    val a = remember(key) { Animatable(if (instant) 1f else 0.35f) }
    LaunchedEffect(key) {
        if (!instant) {
            delay(index.coerceAtMost(6) * 15L)
            a.animateTo(1f, tween(160))
        }
        seen += key
    }
    return graphicsLayer {
        alpha = a.value
        translationY = (1f - a.value) * 14.dp.toPx()
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val rough = remember { entryRough(77) }
    FeldCard(
        modifier = modifier.fillMaxWidth(),
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
private fun ViewToggle(avatarMode: Boolean, onClick: () -> Unit) {
    val rough = remember { entryRough(78) }
    val haptics = rememberFeldHaptics()
    val tint = if (avatarMode) FeldPalette.Orange else FeldPalette.Muted
    FeldCard(
        modifier = Modifier.size(48.dp),
        seed = 78,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(0.dp),
        showDecals = false,
        onClick = {
            haptics.tick()
            onClick()
        },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (avatarMode) {
                Canvas(Modifier.size(22.dp)) {
                    val s = Stroke(1.6.dp.toPx(), join = StrokeJoin.Round)
                    outlineDiamondPath(size.width, size.height, null).let {
                        drawPath(it, tint, style = s)
                    }
                }
            } else {
                FeldIcon(FeldIconId.Image, size = 22.dp, tint = tint)
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
    avatarMode: Boolean,
    onSelect: (Speaker) -> Unit,
    onEdit: (Speaker) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = top),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        pair.forEachIndexed { ci, speaker ->
            val variant = (rowIndex * 2 + ci) % CHIP_VARIANTS
            SpeakerChip(
                speaker = speaker,
                seed = CHIP_SEED_BASE + variant,
                rough = roughs[variant],
                avatarMode = avatarMode,
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
    avatarMode: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
) {
    val color = Color(speaker.colorArgb)
    val iconUri = speaker.customIconUri
    val hasAvatar = avatarMode && iconUri != null
    val density = LocalDensity.current
    val targetPx = with(density) { AvatarHeight.roundToPx() }

    val cached = remember(iconUri) { iconUri?.let { avatarCache.get(it) } }
    val avatar by produceState<ImageBitmap?>(cached, iconUri, hasAvatar) {
        value = if (hasAvatar) {
            value ?: withContext(Dispatchers.IO) { decodeAvatar(iconUri!!, targetPx) }
        } else {
            null
        }
    }
    val avatarAlpha by animateFloatAsState(if (avatar != null) 1f else 0f, tween(180), label = "avatar")
    val tilt = remember(speaker.id) {
        (Random(speaker.id.hashCode()).nextFloat() - 0.5f) * 8f
    }
    val seedHash = speaker.id.hashCode()
    val startPad by animateFloatAsState(if (hasAvatar) 46f else 12f, tween(160), label = "pad")

    Box(Modifier.weight(1f)) {
        FeldCard(
            modifier = Modifier.fillMaxWidth(),
            seed = seed,
            spec = FeldPaper.Entry,
            rough = rough,
            contentPadding = PaddingValues(start = startPad.dp, end = if (onEdit != null) 2.dp else 10.dp),
            showDecals = false,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChipHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!hasAvatar || avatar == null) {
                    Canvas(Modifier.size(12.dp)) {
                        drawPath(
                            outlineDiamondPath(size.width, size.height, seedHash),
                            color,
                            style = Stroke(1.3.dp.toPx(), join = StrokeJoin.Round),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                val base = MaterialTheme.typography.labelMedium
                Text(
                    text = speaker.name.uppercase(),
                    style = base.copy(lineHeight = 1.0.em),
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

        avatar?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-4).dp, y = (-3).dp)
                    .size(AvatarWidth, AvatarHeight)
                    .graphicsLayer {
                        alpha = avatarAlpha
                        rotationZ = tilt
                        scaleX = 0.85f + 0.15f * avatarAlpha
                        scaleY = 0.85f + 0.15f * avatarAlpha
                    }
                    .shadow(4.dp, RoundedCornerShape(4.dp), clip = false)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

private fun outlineDiamondPath(w: Float, h: Float, seed: Int?): Path {
    val j = if (seed == null) {
        FloatArray(8)
    } else {
        val r = Random(seed * 131 + 5)
        FloatArray(8) { (r.nextFloat() - 0.5f) * 0.07f }
    }
    return Path().apply {
        moveTo(w * (0.5f + j[0]), h * (0.04f + abs(j[1])))
        lineTo(w * (0.96f - abs(j[2])), h * (0.5f + j[3]))
        lineTo(w * (0.5f + j[4]), h * (0.96f - abs(j[5])))
        lineTo(w * (0.04f + abs(j[6])), h * (0.5f + j[7]))
        close()
    }
}

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
