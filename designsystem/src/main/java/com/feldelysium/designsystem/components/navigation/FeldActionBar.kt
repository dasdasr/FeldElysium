package com.feldelysium.designsystem.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.button.FeldButton
import com.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
public fun FeldActionBar(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    seed: Int = rememberSeed(),
    divider: Boolean = true,
) {
    val hasConfirm = confirmText != null && onConfirm != null

    Column(modifier.fillMaxWidth()) {
        if (divider) FeldDivider(seed = seed)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (hasConfirm) Arrangement.spacedBy(12.dp) else Arrangement.End,
        ) {
            Text(
                text = cancelText.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                color = FeldPalette.Muted,
                modifier = Modifier
                    .feldClickable(onClick = onCancel)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            )
            if (hasConfirm) {
                FeldButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    spec = FeldButtonDefaults.Orange,
                    enabled = confirmEnabled,
                    minHeight = 52.dp,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 260, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldActionBarPreview() {
    FeldTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldActionBar(onCancel = {}, confirmText = "Confirm", onConfirm = {}, cancelText = "Cancel", seed = 1)
            FeldActionBar(onCancel = {}, cancelText = "Cancel", seed = 7)
        }
    }
}
