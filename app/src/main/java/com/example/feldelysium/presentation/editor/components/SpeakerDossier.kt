package com.example.feldelysium.presentation.editor.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.card.beatScale
import com.example.feldelysium.designsystem.components.card.drawDiamondBeat
import com.example.feldelysium.designsystem.components.card.entryRough
import com.example.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.wornBrush
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette

object SpeakerDossierAssets {
    @DrawableRes
    val Frame: Int? = R.drawable.speaker_frame

    @DrawableRes
    val FramePressed: Int? = R.drawable.speaker_frame_pressed

    @DrawableRes
    val Sticker: Int? = R.drawable.speaker_sticker

    @DrawableRes
    val ChangeBtn: Int? = R.drawable.speaker_change_btn

    @DrawableRes
    val ChangeBtnPressed: Int? = R.drawable.speaker_change_btn_pressed

    @DrawableRes
    val Paperclip: Int? = R.drawable.paperclip

    @DrawableRes
    val PreviewClip: Int? = null
}

private val FrameInset = 20.dp
private val Cream = Color(0xFFE6DCC4)

@Composable
private fun AssetImage(@DrawableRes res: Int?, modifier: Modifier, placeholder: DrawScope.() -> Unit) {
    if (res != null) {
        Image(painterResource(res), null, modifier, contentScale = ContentScale.FillBounds)
    } else {
        Box(modifier.drawBehind(placeholder))
    }
}

private fun dossierCaption(number: Int, name: String): String {
    val initials = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(4)
        .joinToString("") { "${it.first().uppercaseChar()}." }
        .ifEmpty { "N/A" }
    return "NO. ${"%02d".format(number)} // $initials"
}

@Composable
fun SpeakerPhotoBlock(
    iconRes: Int?,
    iconUri: String?,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    name: String = "",
    number: Int = 1,
) {
    val a = SpeakerDossierAssets
    val photoSource = remember { MutableInteractionSource() }
    val photoPressed by photoSource.collectIsPressedAsState()
    val press by animateFloatAsState(if (photoPressed) 1f else 0f, tween(90), label = "photoPress")

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
                .graphicsLayer {
                    rotationZ = -1.2f
                    val s = 1f - 0.015f * press
                    scaleX = s
                    scaleY = s
                }
                .clickable(
                    interactionSource = photoSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(FrameInset)
                    .background(Color(0xFF16201F)),
            ) {
                when {
                    iconUri != null -> AsyncImage(
                        iconUri,
                        null,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    iconRes != null -> Image(
                        painterResource(iconRes),
                        null,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FeldHandDiamond(accent, 3, 30.dp)
                    }
                }
            }
            AssetImage(
                if (photoPressed) a.FramePressed ?: a.Frame else a.Frame,
                Modifier.matchParentSize(),
            ) { drawRect(Cream, style = Stroke(FrameInset.toPx())) }
        }

        Text(
            text = dossierCaption(number, name),
            style = FeldDecalType.Base.copy(
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
            ),
            color = FeldPalette.Muted.copy(alpha = 0.55f),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
        )
    }
}

@Composable
fun SpeakerPreview(name: String, color: Color, seed: Int, modifier: Modifier = Modifier) {
    val rough = remember(seed) { entryRough(seed) }
    val a = SpeakerDossierAssets
    val density = LocalDensity.current.density

    val ringBrush = remember(color, seed, density) {
        wornBrush(color, seed + 2, density, wear = 0.4f, scale = 10f)
    }

    val pulse = rememberInfiniteTransition(label = "previewBeat").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "previewPulse",
    )

    Box(modifier.fillMaxWidth()) {
        FeldCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            seed = seed,
            spec = FeldPaper.Entry,
            rough = rough,
            ambient = 0.3f,
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 22.dp,
            ),
        ) {
            Column {
                Text(
                    "› PREVIEW",
                    style = FeldDecalType.Base.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                    ),
                    color = FeldPalette.Muted,
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .width(7.dp)
                            .height(58.dp)
                            .drawBehind {
                                drawRect(color.copy(alpha = 0.9f))
                            },
                    )

                    Spacer(Modifier.width(16.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .drawBehind {
                                    drawDiamondBeat(
                                        brush = ringBrush,
                                        hold = 0f,
                                        t = pulse.value,
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            FeldHandDiamond(
                                color = color,
                                seed = seed,
                                size = 22.dp,
                                modifier = Modifier.graphicsLayer {
                                    val s = beatScale(pulse.value)
                                    scaleX = s
                                    scaleY = s
                                },
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = name.ifBlank { "Speaker name" }.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Text(
                                text = "“Sample dialogue line…”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                    }
                }
            }
        }

        AssetImage(
            a.PreviewClip ?: a.Paperclip,
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 0.dp)
                .size(14.dp, 44.dp),
        ) {
            drawRoundRect(
                Color(0xFFB06A3A),
                style = Stroke(1.5.dp.toPx()),
            )
        }
    }
}

@Composable
fun FieldLabel(text: String, optional: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            append(text.uppercase())
            if (optional) {
                withStyle(
                    SpanStyle(
                        fontFamily = FeldDecalType.Mono,
                        fontWeight = FontWeight.Normal,
                        color = FeldPalette.Muted.copy(alpha = 0.7f),
                    ),
                ) { append("  (OPTIONAL)") }
            }
        },
        style = FeldDecalType.Label.copy(fontSize = 11.sp, letterSpacing = 1.4.sp),
        color = FeldPalette.Muted,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Preview(
    name = "Speaker Photo Block",
    showBackground = true,
    backgroundColor = 0xFF101716,
)
@Composable
private fun SpeakerPhotoBlockPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF101716))
                .padding(24.dp)
                .width(180.dp),
        ) {
            SpeakerPhotoBlock(
                iconRes = null,
                iconUri = null,
                accent = Color(0xFF3FA58E),
                onClick = {},
            )
        }
    }
}
