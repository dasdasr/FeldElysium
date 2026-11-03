package com.feldelysium.app.presentation.editor.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.eggs.EggEvent
import com.feldelysium.app.eggs.ui.LocalEasterEggs
import com.feldelysium.designsystem.components.container.FeldNoteCard
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.FeldMenu
import com.feldelysium.designsystem.components.input.FeldMenuItem
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun EntryList(
    entries: List<DialogueEntry>,
    fontFamily: FontFamily,
    onEdit: (DialogueEntry) -> Unit,
    onDelete: (String) -> Unit,
    onReorder: (List<DialogueEntry>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current
    var menuEntryId by remember { mutableStateOf<String?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        haptics.handleMove()
        onReorder(entries.toMutableList().apply { add(to.index, removeAt(from.index)) })
    }

    fun delete(entry: DialogueEntry) {
        haptics.thud()
        eggs?.report(EggEvent.EntryDelete, entry.speaker.name)
        onDelete(entry.id)
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize().testTag("entry_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
            ReorderableItem(reorderState, key = entry.id) { isDragging ->
                LaunchedEffect(isDragging) { if (isDragging) haptics.thud() }

                val selected = menuEntryId == entry.id

                Box {
                    FeldNoteCard(
                        title = entry.speaker.name,
                        accent = Color(entry.speaker.colorArgb),
                        text = entry.text,
                        index = index + 1,
                        ambient = 0.7f,
                        seed = entry.id.hashCode(),
                        isDragging = isDragging,
                        selected = selected,
                        dragModifier = Modifier.longPressDraggableHandle(),
                        fontFamily = fontFamily,
                        onClick = {
                            haptics.tick()
                            menuEntryId = entry.id
                        },
                        onSwipeArmed = { haptics.tick() },
                        onSwipeDelete = { delete(entry) },
                    )

                    Box(Modifier.align(Alignment.Center)) {
                        FeldMenu(
                            expanded = selected,
                            onDismiss = { menuEntryId = null },
                            seed = entry.id.hashCode(),
                            offset = DpOffset(0.dp, 0.dp),
                            items = listOf(
                                FeldMenuItem(
                                    stringResource(R.string.editor_menu_edit),
                                    FeldIconId.Edit,
                                    onClick = { onEdit(entry) },
                                ),
                                FeldMenuItem(
                                    stringResource(R.string.editor_menu_delete),
                                    FeldIconId.Delete,
                                    destructive = true,
                                ) { delete(entry) },
                            ),
                        )
                    }
                }
            }
        }
    }
}
