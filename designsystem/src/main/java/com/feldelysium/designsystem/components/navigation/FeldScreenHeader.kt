package com.feldelysium.designsystem.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.button.FeldIconButton
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.content.FeldDiamond
import com.feldelysium.designsystem.components.content.FeldDistressedTitle
import com.feldelysium.designsystem.components.content.FeldDistressedTitles
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.DistressSpec
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
public fun FeldScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    metaStart: String? = null,
    metaEnd: String? = null,
    navIcon: FeldIconId? = FeldIconId.Back,
    onNav: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onDiamondClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    titleSingleLine: Boolean = false,
    titleSpec: DistressSpec = FeldDistressedTitles.Large,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = 1.sp,
    ),
    divider: Boolean = true,
) {
    val haptics = rememberFeldHaptics()
    val tap = if (onClick != null) {
        Modifier.feldClickable(onClick = {
            haptics.tick()
            onClick()
        })
    } else {
        Modifier
    }

    Column(
        modifier
            .fillMaxWidth()
            .then(tap)
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navIcon != null) FeldIconButton(navIcon, onNav)
            if (eyebrow != null) {
                Box(
                    modifier = Modifier
                        .offset(x = (-6).dp)
                        .size(width = 22.dp, height = 24.dp)
                        .feldClickable(onClick = {
                            haptics.tick()
                            onDiamondClick?.invoke()
                        }),
                    contentAlignment = Alignment.Center,
                ) { FeldDiamond(FeldPalette.Orange, size = 10.dp) }
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }

        Spacer(Modifier.height(6.dp))
        FeldDistressedTitle(
            text = title,
            spec = titleSpec,
            textStyle = titleStyle,
            maxLines = if (titleSingleLine) 1 else Int.MAX_VALUE,
            overflow = if (titleSingleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
        )

        if (subtitle != null) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (metaStart != null || metaEnd != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metaStart?.let {
                    Text(
                        it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                }
                FeldDivider(Modifier.weight(1f).padding(horizontal = 12.dp))
                metaEnd?.let {
                    Text(
                        it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                }
            }
        }

        if (divider) {
            Spacer(Modifier.height(12.dp))
            FeldDivider(thickness = 1.dp)
        }
    }
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldScreenHeaderPreview() {
    FeldTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldScreenHeader(
                title = "The final interview",
                eyebrow = "Scene editor",
                metaStart = "4 / 12",
                metaEnd = "Dialogue scene",
                actions = { FeldIconButton(FeldIconId.More, onClick = {}) },
            )
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Choose speaker",
                subtitle = "Select who will say this line.",
                navIcon = FeldIconId.Close,
            )
        }
    }
}
