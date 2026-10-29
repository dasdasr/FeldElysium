package com.example.feldelysium.presentation.settings.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.feldelysium.R
import com.example.feldelysium.data.speaker.SpeakerStorage
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.container.FeldDialog
import com.example.feldelysium.designsystem.components.content.FeldIcon
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.input.feldClickable
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.PaperSpec
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val COLUMNS = 5

private val Cream = Color(0xFFE6DCC4)
private val Dark = Color(0xFF1B1612)

private val TileRough = RoughStyle(
    roughness = 0.45.dp,
    step = 48.dp,
    corner = 2.dp,
    tornChance = 0.04f,
)

private sealed interface IconTile {
    data object Add : IconTile
    data class Custom(val uri: String) : IconTile
    data class Res(val id: Int) : IconTile
}

@Composable
fun ChooseImageDialog(
    iconResIds: List<Int>,
    selectedResId: Int?,
    selectedUri: String?,
    onConfirm: (resId: Int?, uri: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var resId by remember { mutableStateOf(selectedResId) }
    var uri by remember { mutableStateOf(selectedUri) }
    val iconStore = remember { SpeakerStorage.icons(context) }
    var customUris by remember { mutableStateOf(iconStore.allUris()) }

    LaunchedEffect(Unit) {
        customUris = withContext(Dispatchers.IO) { iconStore.allUris() }
    }

    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { picked ->
        if (picked != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { iconStore.copyIn(picked) }
                if (saved != null) {
                    uri = saved
                    resId = null
                    customUris = listOf(saved) + customUris.filter { it != saved }
                }
            }
        }
    }

    val tiles = buildList<IconTile> {
        add(IconTile.Add)
        customUris.forEach { add(IconTile.Custom(it)) }
        iconResIds.forEach { add(IconTile.Res(it)) }
    }

    FeldDialog(
        title = stringResource(R.string.choose_icon_title),
        eyebrow = stringResource(R.string.choose_icon_eyebrow),
        subtitle = stringResource(R.string.choose_icon_subtitle),
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Confirm",
                onConfirm = { onConfirm(resId, uri) },
                onCancel = onDismiss,
                cancelLabel = stringResource(R.string.common_cancel),
            )
        },
    ) {
        tiles.chunked(COLUMNS).forEachIndexed { _, rowTiles ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(COLUMNS) { c ->
                    val tile = rowTiles.getOrNull(c)
                    val tileModifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.8f)

                    when (tile) {
                        null -> Spacer(tileModifier)

                        IconTile.Add -> TileBox(
                            modifier = tileModifier,
                            spec = FeldPaper.DryButtonCream,
                            onClick = {
                                picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                FeldIcon(
                                    id = FeldIconId.Add,
                                    markerFilmSpec = FeldMarker.Ink,
                                    size = 12.dp,
                                )
                                Text(
                                    text = stringResource(R.string.choose_icon_add_custom),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        lineHeight = 10.sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                    color = Dark,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp),
                                )
                            }
                        }

                        is IconTile.Custom -> {
                            val selected = uri == tile.uri
                            TileBox(
                                modifier = tileModifier,
                                spec = selectedSpec(selected),
                                onClick = {
                                    if (selected) {
                                        uri = null
                                    } else {
                                        uri = tile.uri
                                        resId = null
                                    }
                                },
                            ) {
                                AsyncImage(
                                    model = tile.uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                )
                            }
                        }

                        is IconTile.Res -> {
                            val selected = resId == tile.id && uri == null
                            TileBox(
                                modifier = tileModifier,
                                spec = selectedSpec(selected),
                                onClick = {
                                    resId = if (selected) null else tile.id
                                    uri = null
                                },
                            ) {
                                Image(
                                    painter = painterResource(tile.id),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun selectedSpec(selected: Boolean): PaperSpec =
    if (selected) FeldPaper.Field.copy(ink = Cream, inkWidth = 2.dp) else FeldPaper.Field

@Composable
private fun TileBox(
    modifier: Modifier,
    spec: PaperSpec,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val seed = rememberSeed()
    val shape = remember(seed) { RoughShape(seed * 3 + 1, TileRough) }
    Box(
        modifier = modifier
            .paperMaterial(shape, spec)
            .feldClickable(role = Role.Button, onClick = onClick),
        content = content,
    )
}
