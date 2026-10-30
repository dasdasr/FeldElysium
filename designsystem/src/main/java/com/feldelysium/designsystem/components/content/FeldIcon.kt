package com.feldelysium.designsystem.components.content

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.R
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.MarkerFilmSpec
import com.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.feldelysium.designsystem.theme.DiscoDialogueTheme

enum class FeldIconId {
    Back,
    Accept,
    BackingCube,
    BackingCircle,
    More,
    MoreHoriz,
    Import,
    Play,
    Edit,
    Delete,
    Add,
    Close,
    Chevron,
    DialogueStyle,
    Font,
    Palette,
    Reals,
    FastAdd,
    Tag,
    Voice,
    Image,
    Cinema,
    PlaybackText,
    Settings,
    Export,
    BulkImport,
    Video,
    AuthorSamples,
    Typeface,
    Language,
    Save,
    Sound,
    Microphone,
    Autoplay,
    Speed,
    Panel,
    PanelCalendar,
    Calendar,
    Time,
    Logo,
    Heart,
    Paste,
    Search,
    Casino,
    Oprions,
    RadioButtonChecked,
    RadioButtonUnchecked,
}

@Composable
fun FeldIcon(
    id: FeldIconId,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
    seed: Int? = null,
    markerFilmSpec: MarkerFilmSpec = FeldMarker.Chalk,
    film: Boolean = true,
) {
    @DrawableRes val res: Int = when (id) {
        FeldIconId.Back -> R.drawable.ic_fe_back
        FeldIconId.Accept -> R.drawable.ic_fe_accept
        FeldIconId.BackingCube -> R.drawable.ic_fe_backing_cube
        FeldIconId.BackingCircle -> R.drawable.ic_fe_backing_circle
        FeldIconId.Import -> R.drawable.ic_fe_edit_import
        FeldIconId.Play -> R.drawable.ic_fe_play
        FeldIconId.Edit -> R.drawable.ic_fe_edit
        FeldIconId.Delete -> R.drawable.ic_fe_trash
        FeldIconId.Add -> R.drawable.ic_fe_add
        FeldIconId.Tag -> R.drawable.ic_fe_tag
        FeldIconId.AuthorSamples -> R.drawable.ic_fe_author_samples
        FeldIconId.Close -> R.drawable.ic_fe_cancel
        FeldIconId.Chevron -> R.drawable.ic_fe_chevron
        FeldIconId.BulkImport -> R.drawable.ic_fe_bulk_import
        FeldIconId.Typeface -> R.drawable.ic_fe_typeface
        FeldIconId.Autoplay -> R.drawable.ic_fe_autoplay
        FeldIconId.Font -> R.drawable.ic_fe_typeface
        FeldIconId.Video -> R.drawable.ic_fe_video
        FeldIconId.FastAdd -> R.drawable.ic_fe_fast_add
        FeldIconId.Voice -> R.drawable.ic_fe_microphone
        FeldIconId.Image -> R.drawable.ic_fe_image
        FeldIconId.Cinema -> R.drawable.ic_fe_tape
        FeldIconId.Settings -> R.drawable.ic_fe_settings
        FeldIconId.Export -> R.drawable.ic_fe_export
        FeldIconId.Save -> R.drawable.ic_fe_save
        FeldIconId.Sound -> R.drawable.ic_fe_sound
        FeldIconId.Microphone -> R.drawable.ic_fe_microphone
        FeldIconId.More -> R.drawable.ic_fe_more
        FeldIconId.MoreHoriz -> R.drawable.ic_fe_more_horiz
        FeldIconId.DialogueStyle -> R.drawable.ic_fe_dialog
        FeldIconId.Palette -> R.drawable.ic_fe_palette
        FeldIconId.PlaybackText -> R.drawable.ic_fe_playback_text
        FeldIconId.Speed -> R.drawable.ic_fe_speed
        FeldIconId.Panel -> R.drawable.ic_fe_panel
        FeldIconId.PanelCalendar -> R.drawable.ic_fe_panel_calendar
        FeldIconId.Logo -> R.drawable.ic_fe_feld_elysium
        FeldIconId.Heart -> R.drawable.ic_fe_heart
        FeldIconId.Paste -> R.drawable.ic_fe_paste
        FeldIconId.Search -> R.drawable.ic_fe_search
        FeldIconId.Casino -> R.drawable.ic_fe_dice_5
        FeldIconId.Oprions -> R.drawable.ic_fe_options
        FeldIconId.RadioButtonChecked -> R.drawable.ic_fe_radio_button_cheked
        FeldIconId.RadioButtonUnchecked -> R.drawable.ic_fe_radio_button_uncheked
        FeldIconId.Reals -> R.drawable.ic_fe_real
        FeldIconId.Calendar -> R.drawable.ic_fe_calendar
        FeldIconId.Time -> R.drawable.ic_fe_time
        FeldIconId.Language -> R.drawable.ic_fe_language
    }
    Icon(
        painter = painterResource(res),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .then(
                if (film) {
                    Modifier.markerFilmMaterial(
                        markerFilmSpec,
                        id.ordinal + (seed ?: 0),
                        bakeKey = seed,
                    )
                } else {
                    Modifier
                },
            ),
        tint = tint,
    )
}

@Composable
fun FeldDiamond(color: Color, modifier: Modifier = Modifier, size: Dp = 28.dp, outline: Boolean = false) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w, h / 2f)
            lineTo(w / 2f, h)
            lineTo(0f, h / 2f)
            close()
        }
        if (outline) {
            drawPath(path, color, style = Stroke(1.5.dp.toPx()))
        } else {
            drawPath(path, color)
        }
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
