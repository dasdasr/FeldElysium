package com.feldelysium.designsystem.components.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.content.FeldTitleTag
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.FeldTape
import com.feldelysium.designsystem.foundation.material.PaperSpec
import com.feldelysium.designsystem.foundation.material.StickerTapeSpec
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
internal fun FeldDialogCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null,
    seed: Int = rememberSeed(),
    tagSeed: Int = seed + 1,
    spec: PaperSpec = FeldPaper.Panel,
    rough: RoughStyle = entryRough(seed),
    tapeSpec: StickerTapeSpec = FeldTape.Cream,
    contentPadding: PaddingValues = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp),
    tagStart: Dp = 6.dp,
    tagLift: Dp = 12.dp,
    tagHeight: Dp = 44.dp,
    revealOnEnter: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        FeldCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = tagLift),
            seed = seed,
            spec = spec,
            rough = rough,
            contentPadding = PaddingValues(0.dp),
            revealOnEnter = revealOnEnter,
        ) {
            Column(Modifier.padding(contentPadding)) {
                Spacer(Modifier.height(tagHeight - tagLift + 12.dp))
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(14.dp))
                }
                content()
            }
        }

        FeldTitleTag(
            text = title,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = tagStart),
            seed = tagSeed,
            spec = tapeSpec,
        )

        if (onClose != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = tagLift + 8.dp, end = 10.dp)
                    .size(36.dp)
                    .feldClickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                FeldIcon(FeldIconId.Close, size = 20.dp)
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldDialogCardPreview() {
    FeldTheme {
        FeldDialogCard(
            title = "Bulk import",
            subtitle = "Paste your dialogue lines. One per line.",
            onClose = {},
            seed = 5,
            modifier = Modifier.padding(16.dp),
        ) {
            Text("LOGIC: Let's be precise.", color = colorScheme.onSurface)
            Text("HARRY: I remember the cold.", color = colorScheme.onSurface)
        }
    }
}
