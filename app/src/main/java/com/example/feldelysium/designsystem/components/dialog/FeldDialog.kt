package com.example.feldelysium.designsystem.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feldelysium.designsystem.components.display.FeldDiamond
import com.example.feldelysium.designsystem.components.FeldDistressedTitle
import com.example.feldelysium.designsystem.components.FeldDistressedTitles
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.components.button.FeldButton
import com.example.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.foundation.decal.DecalSpec
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed

@Composable
fun FeldDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    ambient: Float? = 0.6f,
    seed: Int = rememberSeed(),
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        FeldCard(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxWidth(),
            ambient = ambient,
            decalSpec = DecalSpec.Dialog,
            seed = seed,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 0.dp),
            revealOnEnter = true,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (eyebrow != null) {
                    FeldDiamond(FeldPalette.Orange, size = 10.dp)
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                FeldIconButton(FeldIconId.Close, onDismiss)
            }

            FeldDistressedTitle(
                text = title,
                seed = seed + 1,
                spec = FeldDistressedTitles.Large,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    letterSpacing = 1.sp,
                ),
            )

            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Spacer(Modifier.height(14.dp))
            FeldDivider(seed = seed + 2)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(16.dp))
                content()
                Spacer(Modifier.height(8.dp))
            }

            FeldDivider(seed = seed + 3)
            footer()
        }
    }
}

@Composable
fun FeldDialogFooter(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    cancelLabel: String = "RESET",
    confirmLabel: String = "SAVE",
    seed: Int = rememberSeed(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (onReset != null) {
                Text(
                    text = cancelLabel,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .feldClickable(onClick = onReset)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        FeldButton(
            confirmLabel,
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            spec = FeldButtonDefaults.Orange,
            seed = seed,
        )
    }
}
