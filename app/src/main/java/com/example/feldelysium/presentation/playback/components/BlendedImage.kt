package com.example.feldelysium.presentation.playback.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

@Composable
fun BlendedImage(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    blendMode: BlendMode = BlendMode.SrcOver,
    bottomFit: Boolean = false,
    colorFilter: ColorFilter? = null,
    alpha: () -> Float = { 1f },
) {
    val image = ImageBitmap.imageResource(res)
    Spacer(
        modifier.drawBehind {
            val w = size.width.roundToInt()
            val h = size.height.roundToInt()
            val fitH = (image.height * size.width / image.width).roundToInt()
            drawImage(
                image = image,
                dstOffset = if (bottomFit) IntOffset(0, h - fitH) else IntOffset.Zero,
                dstSize = if (bottomFit) IntSize(w, fitH) else IntSize(w, h),
                alpha = alpha(),
                colorFilter = colorFilter,
                blendMode = blendMode,
            )
        },
    )
}
