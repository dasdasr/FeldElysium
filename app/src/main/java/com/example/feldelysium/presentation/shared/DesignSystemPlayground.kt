package com.example.feldelysium.presentation.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldCard
import com.example.feldelysium.designsystem.components.FeldRough
import com.example.feldelysium.designsystem.components.FeldTextField
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DialogueText
import com.example.feldelysium.theme.DiscoDialogueTheme
import com.example.feldelysium.theme.DiscoPrimary

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
                seed = 8,
                spec = FeldPaper.Panel,
                rough = RoughStyle(
                    roughness = 1.2.dp,
                    irregularity = 0.65f,
                    smoothness = 0.35f,
                    step = 56.dp,
                    slide = 0.12f,
                    corner = 10.dp,
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
                    inkJump = 0.45f,
                    inkNoiseStep = 36.dp,
                ),
                contentPadding = PaddingValues(16.dp),
                revealOnEnter = false,
                revealWear = 0.65f,
                revealMillis = 700,
                pressScale = 0.012f,
                pressMillis = 90,
                time = { 0f },
                onClick = {},
            ) {
                Text(
                    text = "LOGIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiscoPrimary,
                )

                Text(
                    text = "Let's be precise. What do you actually remember about that night?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DialogueText,
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
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = DialogueText),
                contentPadding = PaddingValues(16.dp),
                time = { 0f },
            )
        }
    }
}
