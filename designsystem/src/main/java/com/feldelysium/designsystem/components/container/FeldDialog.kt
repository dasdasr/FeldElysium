package com.feldelysium.designsystem.components.container

import android.view.WindowManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.feldelysium.designsystem.components.button.FeldButton
import com.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.feldelysium.designsystem.components.button.FeldIconButton
import com.feldelysium.designsystem.components.content.FeldDistressedTitle
import com.feldelysium.designsystem.components.content.FeldDistressedTitles
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.decal.DecalSpec
import com.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.feldelysium.designsystem.foundation.material.DryPaperSpec
import com.feldelysium.designsystem.foundation.material.wornBrush
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
public fun FeldDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    @Suppress("UNUSED_PARAMETER") subtitle: String? = null,
    headerNote: String? = "Feld Elysium\n// UI system",
    ambient: Float? = 0.6f,
    seed: Int = rememberSeed(),
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val view = LocalView.current

        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.setDimAmount(0.75f)
            }
        }

        FeldCard(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxWidth(),
            ambient = ambient,
            decalSpec = DecalSpec.Dialog,
            seed = seed,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 0.dp),
            revealOnEnter = true,
        ) {
            FeldDialogHeader(title, onDismiss, eyebrow, headerNote, seed)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(18.dp))
                content()
                Spacer(Modifier.height(8.dp))
            }

            if (footer != null) {
                FeldDivider(seed = seed + 3)
                footer()
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FeldDialogHeader(title: String, onDismiss: () -> Unit, eyebrow: String?, note: String?, seed: Int) {
    val density = LocalDensity.current.density
    val underline = remember(seed, density) {
        wornBrush(FeldPalette.Orange, seed + 4, density, wear = 0.5f, scale = 8f)
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeldIconButton(FeldIconId.Back, onDismiss)
            if (eyebrow != null) {
                Text(
                    eyebrow.uppercase(),
                    style = FeldDecalType.Base.copy(fontSize = 12.sp, letterSpacing = 1.6.sp),
                    color = FeldPalette.Cream,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            if (note != null) {
                Text(
                    note.uppercase(),
                    style = FeldDecalType.Base.copy(
                        fontSize = 7.sp,
                        lineHeight = 10.sp,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.End,
                    ),
                    color = FeldPalette.Muted.copy(alpha = 0.55f),
                )
            }
        }
        FeldDivider(seed = seed + 2, segment = 160.dp)
        Spacer(Modifier.height(12.dp))
        FeldDistressedTitle(
            text = title,
            seed = seed + 1,
            spec = FeldDistressedTitles.Large,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 34.sp,
                lineHeight = 36.sp,
                letterSpacing = 1.sp,
            ),
        )
        Box(
            Modifier
                .padding(top = 2.dp, start = 3.dp)
                .width(92.dp)
                .height(3.dp)
                .drawBehind { drawRect(underline, alpha = 0.95f) },
        )
    }
}

@Composable
public fun FeldDialogFooter(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    cancelLabel: String = "RESET",
    confirmLabel: String = "SAVE",
    seed: Int = rememberSeed(),
    buttonSpec: DryPaperSpec = FeldButtonDefaults.Orange,
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
            spec = buttonSpec,
            seed = seed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldDialogPreview() {
    FeldTheme {
        FeldDialog(
            title = "Create Speaker",
            eyebrow = "NEW IDENTITY",
            subtitle = "Give this voice a place in the world",
            onDismiss = {},
            seed = 42,
            footer = {
                FeldDialogFooter(
                    onCancel = {},
                    onReset = {},
                    cancelLabel = "CLEAR",
                    confirmLabel = "SAVE",
                    seed = 42,
                )
            },
        ) {
            Text(
                text = "Every voice leaves something behind.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Choose a name, a colour, and a little personality. Make it memorable.",
                style = MaterialTheme.typography.bodyMedium,
                color = FeldPalette.Muted,
            )

            Spacer(Modifier.height(20.dp))

            FeldDivider(seed = 43)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "IDENTITY RECORD  /  NO. 001",
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Orange,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldDialogMinimalPreview() {
    FeldTheme {
        FeldDialog(
            title = "Are You Sure?",
            eyebrow = "UNSAVED CHANGES",
            onDismiss = {},
            seed = 12,
            footer = {
                FeldDialogFooter(
                    onCancel = {},
                    cancelLabel = "DISCARD",
                    confirmLabel = "KEEP EDITING",
                    seed = 12,
                )
            },
        ) {
            Text(
                text = "Your changes have not been saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldDialogFooterPreview() {
    FeldTheme {
        Column(Modifier.padding(16.dp)) {
            FeldDialogFooter(
                onCancel = {},
                onReset = {},
                cancelLabel = "RESET",
                confirmLabel = "SAVE",
                seed = 1,
            )

            FeldDialogFooter(
                onCancel = {},
                cancelLabel = "CANCEL",
                confirmLabel = "CONFIRM",
                seed = 2,
            )
        }
    }
}
