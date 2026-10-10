package com.example.feldelysium.designsystem.components.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.theme.DiscoDialogueTheme


enum class FeldIconId {
    Back, More, MoreHoriz, Import, Play, Reorder, Edit, Delete, Drag, Add, Close, Chevron,
    DialogueStyle, Font, Palette, Grid, FastAdd, Timer, Voice, Image, Aspect, PlaybackText,
    Settings,
}

@Composable
fun FeldIcon(
    id: FeldIconId,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    film: Boolean = false,
) {
    val v: ImageVector = when (id) {
        FeldIconId.Back -> Icons.AutoMirrored.Filled.ArrowBack
        FeldIconId.More -> Icons.Default.MoreVert
        FeldIconId.MoreHoriz -> Icons.Default.MoreHoriz
        FeldIconId.Import -> Icons.Default.Upload
        FeldIconId.Play -> Icons.Default.PlayArrow
        FeldIconId.Reorder -> Icons.Default.SwapVert
        FeldIconId.Edit -> Icons.Default.Edit
        FeldIconId.Delete -> Icons.Default.Delete
        FeldIconId.Drag -> Icons.Default.DragIndicator
        FeldIconId.Add -> Icons.Default.Add
        FeldIconId.Close -> Icons.Default.Close
        FeldIconId.Chevron -> Icons.AutoMirrored.Filled.KeyboardArrowRight
        FeldIconId.DialogueStyle -> Icons.AutoMirrored.Filled.Article
        FeldIconId.Font -> Icons.Default.TextFields
        FeldIconId.Palette -> Icons.Default.Palette
        FeldIconId.Grid -> Icons.Default.GridOn
        FeldIconId.FastAdd -> Icons.Default.AddBox
        FeldIconId.Timer -> Icons.Default.Timer
        FeldIconId.Voice -> Icons.AutoMirrored.Filled.VolumeUp
        FeldIconId.Image -> Icons.Default.Image
        FeldIconId.Aspect -> Icons.Default.AspectRatio
        FeldIconId.PlaybackText -> Icons.Default.Subtitles
        FeldIconId.Settings -> Icons.Default.Settings
    }
    Icon(
        v,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .then(
                if (film) Modifier.markerFilmMaterial(
                    FeldMarker.Chalk,
                    id.ordinal
                ) else Modifier
            ),
        tint = tint,
    )
}

@Composable
fun FeldDiamond(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    outline: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f); lineTo(w, h / 2f); lineTo(w / 2f, h); lineTo(0f, h / 2f); close()
        }
        if (outline) drawPath(path, color, style = Stroke(1.5.dp.toPx()))
        else drawPath(path, color)
    }
}

@Preview(
    widthDp = 400,
    heightDp = 500,
    showBackground = true,
    backgroundColor = 0xFF0B1011,
)
@Composable
private fun FeldIconPreview() {
    DiscoDialogueTheme {
        val icons = FeldIconId.entries

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(icons) { id ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FeldIcon(
                            id = id,
                            size = 24.dp,
                            tint = Color.White,
                        )

                        Text(
                            text = id.name,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            Spacer(Modifier.size(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeldDiamond(
                    color = Color.White,
                    size = 28.dp,
                )

                FeldDivider(
                    vertical = true,
                    thickness = 2.dp,
                    segment = 15.dp,
                    changeChance = 0.6f,
                )

                FeldDiamond(
                    color = Color.White,
                    size = 28.dp,
                    outline = true,
                )
            }
        }
    }
}
