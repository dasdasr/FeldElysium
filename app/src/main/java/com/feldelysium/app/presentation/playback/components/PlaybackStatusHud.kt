package com.feldelysium.app.presentation.playback.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import kotlin.math.roundToInt
import com.feldelysium.designsystem.R as DesignR

private const val TIME_FONT_SCALE = 2.3f

private const val BASELINE_FROM_BOTTOM_FRACTION = 0.091f

private const val ICON_CENTER_X_FRACTION = 0.666f
private const val MONEY_START_X_FRACTION = 0.691f
private const val TIME_START_X_FRACTION = 0.768f
private const val DAY_START_X_FRACTION = 0.923f

private const val SMALL_FONT_WIDTH_FRACTION = 0.0274f

private const val DIGIT_CENTER_ABOVE_BASELINE_EM = 0.35f

private const val MONEY_BRIGHTNESS = 0.65f
private const val TIME_BRIGHTNESS = 0.85f
private const val DAY_BRIGHTNESS = 0.50f

private val RobotoCondensedLight = FontFamily(
    Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Light),
)

@Composable
fun PlaybackStatusHud(money: String, time: String, day: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val smallFont = with(density) { (maxWidth * SMALL_FONT_WIDTH_FRACTION).toSp() }
        val timeFont = smallFont * TIME_FONT_SCALE
        val iconSize = maxWidth * SMALL_FONT_WIDTH_FRACTION
        val smallFontPx = with(density) { smallFont.toPx() }

        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_fe_real),
                    tint = gray(1f),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
                StatusLabel(text = money, fontSize = smallFont, brightness = MONEY_BRIGHTNESS)
                StatusLabel(text = time, fontSize = timeFont, brightness = TIME_BRIGHTNESS)
                StatusLabel(text = day, fontSize = smallFont, brightness = DAY_BRIGHTNESS)
            },
        ) { measurables, constraints ->
            val unbounded = Constraints(
                maxWidth = Constraints.Infinity,
                maxHeight = Constraints.Infinity,
            )
            val icon = measurables[0].measure(unbounded)
            val moneyLabel = measurables[1].measure(unbounded)
            val timeLabel = measurables[2].measure(unbounded)
            val dayLabel = measurables[3].measure(unbounded)
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val baseline = height - width * BASELINE_FROM_BOTTOM_FRACTION
            layout(width, height) {
                fun placeLabel(placeable: Placeable, startFraction: Float) {
                    placeable.place(
                        x = (width * startFraction).roundToInt(),
                        y = (baseline - placeable[FirstBaseline]).roundToInt(),
                    )
                }
                placeLabel(moneyLabel, MONEY_START_X_FRACTION)
                placeLabel(timeLabel, TIME_START_X_FRACTION)
                placeLabel(dayLabel, DAY_START_X_FRACTION)
                val iconCenterY = baseline - smallFontPx * DIGIT_CENTER_ABOVE_BASELINE_EM
                icon.place(
                    x = (width * ICON_CENTER_X_FRACTION).roundToInt() - icon.width / 2,
                    y = iconCenterY.roundToInt() - icon.height / 2,
                )
            }
        }
    }
}

@Composable
private fun StatusLabel(text: String, fontSize: TextUnit, brightness: Float) {
    Text(
        text = text,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        style = TextStyle(
            color = gray(brightness),
            fontSize = fontSize,
            fontFamily = RobotoCondensedLight,
            lineHeight = fontSize,
        ),
    )
}

private fun gray(brightness: Float): Color {
    val channel = (brightness * 255f).roundToInt().coerceIn(0, 255)
    return Color(channel, channel, channel)
}

@Preview(
    name = "Playback Status HUD",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360,
    heightDp = 100,
)
@Composable
private fun PlaybackStatusHudPreview() {
    PlaybackStatusHud(
        money = "40.95",
        time = "16:36",
        day = "Day 3",
    )
}
