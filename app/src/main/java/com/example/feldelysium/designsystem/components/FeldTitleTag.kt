package com.example.feldelysium.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.example.feldelysium.designsystem.material.FeldTape
import com.example.feldelysium.designsystem.material.StickerTapeSpec
import com.example.feldelysium.designsystem.material.stickerTapeMaterial
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldTitleTag(
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    spec: StickerTapeSpec = FeldTape.Cream,
    contentColor: Color = Color(0xFF1B1612),
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
    letterSpacing = 0.4.sp,
    ),
    rotation: Float = -0.6f,
) {
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation }
            .stickerTapeMaterial(spec, seed)
            .padding(
                start = spec.edgeInset + 4.dp,
                end = spec.edgeInset + 16.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = textStyle, color = contentColor)
    }
}

@Preview(widthDp = 360, heightDp = 240, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldTitleTagPreview() {
    DiscoDialogueTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeldTitleTag("Choose speaker", seed = 1)
            FeldTitleTag("Background settings", seed = 2)
            FeldTitleTag("Entry edit", seed = 14)
        }
    }
}
