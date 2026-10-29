package com.example.feldelysium.presentation.editor.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.container.FeldCard
import com.example.feldelysium.designsystem.components.container.entryRough
import com.example.feldelysium.designsystem.components.content.FeldIcon
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldMenu
import com.example.feldelysium.designsystem.components.input.FeldMenuItem
import com.example.feldelysium.designsystem.components.input.FeldPalette
import com.example.feldelysium.designsystem.components.input.feldClickable
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.domain.model.SceneInfo

private val FeaturedHeight = 214.dp
private val CompactHeight = 188.dp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SceneFolder(
    scene: SceneInfo,
    index: Int,
    featured: Boolean,
    menuOpen: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
) {
    val seed = scene.id.hashCode()
    val rough = remember(seed) { entryRough(seed) }
    val orange = rememberOrangeBrush(seed + 5)
    val renameLabel = stringResource(R.string.scenes_menu_rename)

    val fg = if (featured) Ink else MaterialTheme.colorScheme.onSurface
    val sub = if (featured) Ink.copy(alpha = 0.62f) else FeldPalette.Muted

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
    val lift by animateFloatAsState(
        if (pressed) 1f else 0f,
        tween(90),
        label = "lift",
    )

    val clearLabel = stringResource(R.string.scenes_menu_clear_lines)
    val deleteLabel = stringResource(R.string.common_delete)

    Box(
        Modifier
            .offset(y = dropY)
            .graphicsLayer {
                rotationZ = tilt
                val scale = 1f - 0.025f * lift
                scaleX = scale
                scaleY = scale
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
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = if (featured) Modifier.feldClickable(onClick = onRename) else Modifier,
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
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    if (featured) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .offset(x = (-8).dp, y = (-4).dp)
                                .size(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            FeldIcon(
                                id = FeldIconId.BackingCube,
                                size = 20.dp,
                                markerFilmSpec = FeldMarker.Ink.copy(color = Color(0xFFDCCFB5)),
                            )
                            FeldIcon(
                                id = FeldIconId.Edit,
                                size = 14.dp,
                                markerFilmSpec = FeldMarker.Ink,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.scene_lines_count,
                            scene.lines,
                            scene.lines,
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                        ),
                        color = sub,
                        modifier = Modifier.weight(1f),
                    )

                    if (featured) {
                        Text(
                            text = stringResource(R.string.scenes_hold_for_more),
                            style = MaterialTheme.typography.labelSmall,
                            color = sub.copy(alpha = 0.5f),
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                if (scene.preview.isBlank()) {
                    Text(
                        text = stringResource(R.string.scenes_empty_tape),
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
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                            ),
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
                    .markerFilmMaterial(
                        FeldMarker.Ink.copy(color = Color.Unspecified),
                        seed,
                        bake = false,
                    ),
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
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = ghost,
                lineHeight = ghost * 0.85f,
            ),
            color = fg.copy(alpha = 0.06f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 10.dp,
                    bottom = if (featured) 40.dp else 32.dp,
                ),
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
                    text = stringResource(R.string.common_current),
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
                    add(FeldMenuItem(renameLabel, FeldIconId.Edit, onClick = onRename))
                    if (scene.lines > 0) add(FeldMenuItem(clearLabel, FeldIconId.Close, onClick = onClear))
                    add(FeldMenuItem(deleteLabel, FeldIconId.Delete, destructive = true, onClick = onDelete))
                },
            )
        }
    }
}
