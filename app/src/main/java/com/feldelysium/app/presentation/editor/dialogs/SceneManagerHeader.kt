package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.button.FeldIconButton
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.content.FeldDistressedTitle
import com.feldelysium.designsystem.components.content.FeldDistressedTitles
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.foundation.decal.FeldDecalType

@Composable
internal fun ManagerTopBar(count: Int, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldIconButton(FeldIconId.Back, onBack)

        Text(
            text = stringResource(R.string.scenes_top_bar_title),
            style = FeldDecalType.Base.copy(fontSize = 12.sp, letterSpacing = 1.6.sp),
            color = FeldPalette.Cream,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        Text(
            text = count.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, lineHeight = 28.sp),
            color = FeldPalette.Orange,
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.scenes_saved),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = FeldPalette.Muted,
        )
    }
}

@Composable
internal fun ManagerTitle() {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .padding(start = 2.dp)
                .graphicsLayer {
                    rotationZ = -1.2f
                    transformOrigin = TransformOrigin(0f, 1f)
                },
        ) {
            FeldDistressedTitle(
                text = stringResource(R.string.scenes_title),
                seed = 1,
                spec = FeldDistressedTitles.Large,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 64.sp,
                    lineHeight = 60.sp,
                    letterSpacing = 0.sp,
                ),
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(FeldPalette.Orange),
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = stringResource(R.string.scenes_hint_hold_folder),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                color = FeldPalette.Muted,
            )
        }

        Spacer(Modifier.height(10.dp))
        FeldDivider(seed = 7)
    }
}
