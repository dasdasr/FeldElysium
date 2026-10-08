package com.example.feldelysium.presentation.editor.components

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.PaperSpec
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val Columns = 5

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
fun ChooseIconDialog(
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

    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { picked ->
        if (picked != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { copyIntoApp(context, picked) }
                if (saved != null) {
                    uri = saved
                    resId = null
                }
            }
        }
    }

    val tiles = buildList<IconTile> {
        add(IconTile.Add)
        uri?.let { add(IconTile.Custom(it)) }
        iconResIds.forEach { add(IconTile.Res(it)) }
    }

    FeldDialog(
        title = "Choose icon",
        eyebrow = "Speaker icon",
        subtitle = "Pick a picture for the speaker.",
        onDismiss = onDismiss,
        seed = 24,
        footer = {
            FeldDialogActions(
                confirmLabel = "Confirm",
                onConfirm = { onConfirm(resId, uri) },
                onCancel = onDismiss,
                seed = 12,
            )
        },
    ) {
        tiles.chunked(Columns).forEachIndexed { r, rowTiles ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(Columns) { c ->
                    val tile = rowTiles.getOrNull(c)
                    val tileModifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.8f)
                    val seed = r * Columns + c + 1

                    when (tile) {
                        null -> Spacer(tileModifier)

                        IconTile.Add -> TileBox(
                            modifier = tileModifier,
                            seed = seed,
                            spec = FeldPaper.DryButtonCream,
                            onClick = {
                                picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Dark,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    text = "ADD CUSTOM IMAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                    color = Dark,
                                )
                            }
                        }

                        is IconTile.Custom -> TileBox(
                            modifier = tileModifier,
                            seed = seed,
                            spec = selectedSpec(true),
                            onClick = { uri = null },
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

                        is IconTile.Res -> {
                            val selected = resId == tile.id && uri == null
                            TileBox(
                                modifier = tileModifier,
                                seed = seed,
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
    seed: Int,
    spec: PaperSpec,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = remember(seed) { RoughShape(seed * 3 + 1, TileRough) }
    Box(
        modifier = modifier
            .paperMaterial(shape, spec)
            .feldClickable(role = Role.Button, onClick = onClick),
        content = content,
    )
}

private fun copyIntoApp(context: Context, source: Uri): String? = runCatching {
    val dir = File(context.filesDir, "speaker_icons").apply { mkdirs() }
    val out = File(dir, "${UUID.randomUUID()}.img")
    context.contentResolver.openInputStream(source)?.use { input ->
        out.outputStream().use { output -> input.copyTo(output) }
    } ?: return null
    Uri.fromFile(out).toString()
}.getOrNull()
