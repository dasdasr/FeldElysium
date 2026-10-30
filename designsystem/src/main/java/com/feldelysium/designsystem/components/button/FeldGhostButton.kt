package com.feldelysium.designsystem.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.container.FeldCard
import com.feldelysium.designsystem.components.container.entryRough
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.markerFilmMaterial

@Composable
public fun FeldGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: FeldIconId? = null,
    seed: Int = 31,
) {
    val haptics = rememberFeldHaptics()
    val rough = remember(seed) { entryRough(seed) }

    FeldCard(
        modifier = modifier,
        seed = seed,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(horizontal = 10.dp),
        showDecals = false,
        onClick = {
            haptics.tick()
            onClick()
        },
    ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                FeldIcon(icon, size = 20.dp, markerFilmSpec = FeldMarker.Chalk)
                Spacer(Modifier.width(10.dp))
            }
            Box(Modifier.weight(1f, fill = false).markerFilmMaterial(FeldMarker.Chalk, seed + 1)) {
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
