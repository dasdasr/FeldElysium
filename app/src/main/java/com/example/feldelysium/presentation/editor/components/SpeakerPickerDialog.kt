package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.button.FeldTextButton
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.card.entryRough
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.designsystem.components.settings.FeldSectionHeader
import com.example.feldelysium.designsystem.components.settings.FeldSettingsDivider
import com.example.feldelysium.designsystem.components.speaker.FeldSpeakerDiamond
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private val Dark = Color(0xFF1B1612)

private data class SpeakerGroup(
    val type: SpeakerType,
    val title: String,
    val items: List<Speaker>,
)

@Preview
@Composable
private fun SpeakerPickerDialogPreview() {
    SpeakerPickerDialog(
        speakers = PredefinedSpeakers.ALL + Speaker(
            id = "custom",
            name = "Ancient Reptilian Brain",
            colorArgb = Speaker.DEFAULT_CUSTOM_COLOR,
            type = SpeakerType.CUSTOM,
        ),
        onSpeakerSelected = {},
        onCreateCustom = {},
        onEditCustom = {},
        onDismiss = {},
    )
}

@Composable
fun SpeakerPickerDialog(
    speakers: List<Speaker>,
    onSpeakerSelected: (Speaker) -> Unit,
    onCreateCustom: () -> Unit,
    onEditCustom: (Speaker) -> Unit,
    onDismiss: () -> Unit, modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var collapsed by remember { mutableStateOf(setOf<SpeakerType>()) }

    val q = query.trim()
    val groups = remember(speakers) {
        listOf(
            SpeakerType.SKILL to "Skills",
            SpeakerType.NPC to "NPCs",
            SpeakerType.CUSTOM to "Custom",
        ).map { (type, title) ->
            val items = speakers
                .filter { it.type == type }
                .filter { q.isEmpty() || it.name.contains(q, ignoreCase = true) }
            SpeakerGroup(type, title, items)
        }.filter { it.items.isNotEmpty() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
                .imePadding(),
        ) {
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Choose speaker",
                subtitle = "Select who will say this line.",
                navIcon = FeldIconId.Close,
                onNav = onDismiss,
            )

            SearchField(value = query, onValueChange = { query = it })

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                if (groups.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = "NO SPEAKERS FOUND.",
                            style = MaterialTheme.typography.labelSmall,
                            color = FeldPalette.Muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                        )
                    }
                }

                groups.forEachIndexed { gi, group ->
                    val expanded = q.isNotEmpty() || group.type !in collapsed

                    item(key = "header-${group.type}") {
                        Spacer(Modifier.height(12.dp))
                        FeldSectionHeader(
                            title = group.title,
                            seed = 40 + gi,
                            onClick = {
                                collapsed = if (group.type in collapsed) collapsed - group.type
                                else collapsed + group.type
                            },
                            trailing = {
                                Text(
                                    text = group.items.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FeldPalette.Muted,
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                                    else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = FeldPalette.Muted,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(20.dp),
                                )
                            },
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    if (expanded) {
                        item(key = "table-${group.type}") {
                            SpeakerTable(
                                items = group.items,
                                seed = 60 + gi,
                                onSelect = onSpeakerSelected,
                                onEdit = onEditCustom,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                CreateSpeakerButton(onClick = onCreateCustom)
                FeldTextButton("Cancel", onDismiss)
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = FeldPalette.Muted,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(FeldPalette.Orange),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Search speakers...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = FeldPalette.Muted.copy(alpha = 0.7f),
                        )
                    }
                    inner()
                },
            )
        }
        FeldDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun SpeakerTable(
    items: List<Speaker>,
    seed: Int,
    onSelect: (Speaker) -> Unit,
    onEdit: (Speaker) -> Unit,
) {
    val rough = remember(seed) { entryRough(seed) }
    FeldCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(0.dp),
    ) {
        items.forEachIndexed { i, speaker ->
            if (i > 0) FeldDivider(Modifier.padding(horizontal = 14.dp))
            SpeakerRow(
                speaker = speaker,
                onClick = { onSelect(speaker) },
                onEdit = if (speaker.type == SpeakerType.CUSTOM) {
                    { onEdit(speaker) }
                } else null,
            )
        }
    }
}

@Composable
private fun SpeakerRow(
    speaker: Speaker,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    val color = Color(speaker.colorArgb)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .feldClickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldHandDiamond(color, speaker.id.hashCode(), 20.dp)
        Spacer(Modifier.width(14.dp))
        Text(
            text = speaker.name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.weight(1f),
        )
        if (onEdit != null) {
            Box(
                Modifier.size(36.dp).feldClickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Edit, "Edit",
                    tint = FeldPalette.Muted, modifier = Modifier.size(18.dp),
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = FeldPalette.Muted, modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun CreateSpeakerButton(onClick: () -> Unit) {
    val shape = remember {
        RoughShape(
            33,
            RoughStyle(
                roughness = 0.8.dp, step = 36.dp, corner = 2.dp,
                cornerVariation = 0.3f, cornerJitter = 0.3f,
                tornChance = 0.08f, jerkChance = 0.08f,
            ),
        )
    }
    val spec = remember {
        FeldPaper.DryButtonCream.copy(
            base = Color(0xFFE45720),
            scuff = Color(0xFFB23D10),
            wear = 0.45f,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer { rotationZ = -1.2f }
            .paperMaterial(shape, spec)
            .feldClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, null, tint = Dark, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = "CREATE CUSTOM SPEAKER",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
            ),
            color = Dark,
        )
    }
}
