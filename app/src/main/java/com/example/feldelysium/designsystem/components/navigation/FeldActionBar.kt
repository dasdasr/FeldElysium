package com.example.feldelysium.designsystem.components.navigation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.button.FeldButton
import com.example.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.example.feldelysium.designsystem.components.container.FeldDivider
import com.example.feldelysium.designsystem.components.input.FeldPalette
import com.example.feldelysium.designsystem.components.input.feldClickable
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.theme.DiscoDialogueTheme

@Composable
fun FeldActionBar(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.common_cancel),
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
    DiscoDialogueTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldActionBar(onCancel = {}, confirmText = "Confirm", onConfirm = {}, seed = 1)
            FeldActionBar(onCancel = {}, confirmText = "Import (12)", onConfirm = {}, seed = 4)
            FeldActionBar(onCancel = {}, seed = 7)
        }
    }
}
