package com.feldelysium.app.presentation.playback.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feldelysium.app.R

private const val TAPE_TEXTURE_WIDTH_PX = 614f
private const val TAPE_TEXTURE_HEIGHT_PX = 3598f
private const val TAPE_TEXTURE_ASPECT = TAPE_TEXTURE_HEIGHT_PX / TAPE_TEXTURE_WIDTH_PX

// Tile manually to keep the texture scroll seamless.
@Composable
fun TapeBackground(offsetPx: () -> Float, modifier: Modifier = Modifier) {
    val painter = painterResource(id = R.drawable.seamless_tape)
    Canvas(modifier = modifier) {
        val tileHeight = size.width * TAPE_TEXTURE_ASPECT
        if (tileHeight < 1f || size.height <= 0f) return@Canvas

        val scroll = positiveMod(offsetPx(), tileHeight)
        var top = -scroll
        var guard = 0
        while (top < size.height && guard < 8) {
            translate(top = top) {
                with(painter) {
                    draw(Size(size.width, tileHeight))
                }
            }
            top += tileHeight
            guard++
        }
    }
}

private fun positiveMod(value: Float, modulus: Float): Float {
    val remainder = value % modulus
    return if (remainder < 0f) remainder + modulus else remainder
}

@Preview
@Composable
private fun TapeBackgroundPreview() {
    TapeBackground(
        offsetPx = { 0f },
        modifier = Modifier
            .width(220.dp)
            .height(400.dp),
    )
}
