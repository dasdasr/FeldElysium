package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultPlaque(text: String, color: Color, height: Dp, modifier: Modifier = Modifier) {
    val fontSize = with(LocalDensity.current) { (height * 0.5f).toSp() }
    Box(modifier.height(height), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(CheckAssets.Plaque),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize(),
        )
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontSize = fontSize,
            letterSpacing = 1.5.sp,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = height * 0.6f),
        )
    }
}
