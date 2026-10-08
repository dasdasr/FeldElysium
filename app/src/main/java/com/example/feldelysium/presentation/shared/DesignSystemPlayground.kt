package com.example.feldelysium.presentation.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.button.FeldButton
import com.example.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.example.feldelysium.designsystem.components.FeldDistressedTitle
import com.example.feldelysium.designsystem.components.FeldDistressedTitles
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.input.FeldRough
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.components.card.FeldDialogCard
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun DesignSystemPlayground(
    modifier: Modifier = Modifier,
) {
    var text by remember {
        mutableStateOf(
            "Let's be precise. What do you actually remember about that night?"
        )
    }

    DiscoDialogueTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "DESIGN SYSTEM PLAYGROUND",
                style = MaterialTheme.typography.labelSmall,
            )

            FeldCard(
                modifier = Modifier.fillMaxWidth(),
                seed = 811,
                spec = FeldPaper.Panel,
                rough = RoughStyle(
                    roughness = 1.2.dp,
                    irregularity = 0.65f,
                    smoothness = 0.35f,
                    step = 56.dp,
                    slide = 0.12f,
                    corner = 3.dp,
                    cornerVariation = 0.35f,
                    cornerJitter = 0.5f,
                    inset = 1.5.dp,
                    jerkChance = 0.14f,
                    jerkAmp = 1.3f,
                    jerkWidth = 3.dp,
                    tornChance = 0.18f,
                    forceTorn = -1,
                    tornPush = 0.6f,
                    inkVariation = 0.55f,
                    inkNoiseStep = 36.dp,
                ),
                contentPadding = PaddingValues(16.dp),
                revealOnEnter = true,
                revealMillis = 300,
                pressScale = 0.12f,
                pressMillis = 90,
                time = { 0f },
                onClick = {},
            ) {
                Text(
                    text = "LOGIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                )

                Text(
                    text = "Let's be precise. What do you actually remember about that night?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            FeldTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                seed = 5,
                spec = FeldPaper.Field,
                rough = FeldRough.Field,
                placeholder = "Let's be precise. What do you actually remember about that night?",
                maxLength = 500,
                minLines = 1,
                enabled = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurface),
                contentPadding = PaddingValues(16.dp),
                time = { 0f },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FeldButton("Cancel", {}, Modifier.weight(1f), seed = 21)
                FeldButton(
                    "Save", {}, Modifier.weight(1f),
                    seed = 8,
                    spec = FeldButtonDefaults.Orange,
                    icon = { Icon(Icons.Default.Check, null, Modifier.size(20.dp)) },
                )
            }
            FeldButton(
                "Delete line", {}, Modifier.fillMaxWidth(),
                seed = 3,
                spec = FeldButtonDefaults.Orange,
                minHeight = 72.dp,
                contentAlignment = Alignment.CenterStart,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            )

            FeldTitleTag(
                text = "BACKGROUND",
                distressed = true,
            )

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

            val largeStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 52.sp,
                lineHeight = 54.sp,
                letterSpacing = 1.sp,
            )
            FeldDistressedTitle(
                text = "The final interview",
                seed = 2,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )

            FeldDistressedTitle(
                text = "SMOKE",
                seed = 2,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )
        }
    }
}
