package com.example.feldelysium.presentation.editor.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.input.FeldPalette
import com.example.feldelysium.designsystem.components.input.feldClickable

@Composable
internal fun NewSceneCard(onClick: () -> Unit) {
    val dash = FeldPalette.Muted

    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind {
                val inset = 1.dp.toPx()
                drawRoundRect(
                    color = dash,
                    topLeft = Offset(inset, inset),
                    size = Size(
                        size.width - inset * 2,
                        size.height - inset * 2,
                    ),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10.dp.toPx(), 7.dp.toPx()),
                        ),
                    ),
                )
            }
            .feldClickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 44.sp),
            color = FeldPalette.Cream,
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = stringResource(R.string.scenes_new),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 3.sp,
            ),
            color = FeldPalette.Muted,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = stringResource(R.string.scenes_tap_to_create),
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted.copy(alpha = 0.5f),
        )
    }
}
