package com.example.feldelysium.presentation.editor

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.designsystem.components.FeldFooterBand
import com.example.feldelysium.designsystem.components.FeldIcon
import com.example.feldelysium.designsystem.components.FeldIconId
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.FeldScreenHeader
import com.example.feldelysium.designsystem.components.FeldTapOutsideDismiss
import com.example.feldelysium.designsystem.components.FeldToolbar
import com.example.feldelysium.designsystem.components.FeldToolbarItem
import com.example.feldelysium.designsystem.components.button.FeldButton
import com.example.feldelysium.designsystem.components.button.FeldFab
import com.example.feldelysium.designsystem.components.card.FeldEntryCard
import com.example.feldelysium.designsystem.components.rememberFeldHaptics
import com.example.feldelysium.eggs.LocalEasterEggs
import com.example.feldelysium.presentation.editor.components.BulkImportDialog
import com.example.feldelysium.presentation.editor.components.CustomSpeakerDialog
import com.example.feldelysium.presentation.editor.components.EntryEditSheet
import com.example.feldelysium.presentation.editor.components.SpeakerPickerDialog
import com.example.feldelysium.presentation.shared.SceneViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@SuppressLint("ContextCastToActivity")
@Composable
fun EditorScreen(
    onNavigateToPlayback: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current

    val sceneVm: SceneViewModel = viewModel(activity)
    val vm: EditorViewModel = viewModel(factory = EditorViewModel.factory(sceneVm))
    val state by vm.state.collectAsStateWithLifecycle()

    var menuEntryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                EditorEffect.NavigateToPlayback -> onNavigateToPlayback()
                is EditorEffect.ShowSnackbar ->
                    Toast.makeText(activity, effect.message, Toast.LENGTH_SHORT).show()

                EditorEffect.LaunchGalleryPicker -> { /* TODO */
                }
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val reordered = state.entries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        haptics.handleMove()

        vm.dispatch(EditorEvent.ReorderEntries(reordered))
    }

    FeldTapOutsideDismiss(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            FeldScreenHeader(
                title = state.sceneName,
                eyebrow = "SCENE EDITOR",
                metaStart = "${state.entries.size} LINES",
                metaEnd = "DIALOGUE SCENE",
                navIcon = null,
                onTitleChange = {
                    vm.dispatch(EditorEvent.RenameScene(it.take(60)))
                },
            )

            FeldToolbar(
                items = listOf(
                    FeldToolbarItem(FeldIconId.Import, "Paste") {
                        vm.dispatch(EditorEvent.OpenImportDialog)
                    },
                    FeldToolbarItem(
                        icon = FeldIconId.Play,
                        label = "Playback",
                        enabled = state.entries.isNotEmpty(),
                        onClick = { haptics.confirm(); vm.dispatch(EditorEvent.NavigateToPlayback) },
                    ),
                    FeldToolbarItem(
                        FeldIconId.Settings,
                        "Settings",
                        onClick = onNavigateToSettings
                    ),
                ),
            )

            Box(Modifier.weight(1f)) {
                if (state.entries.isEmpty()) {
                    EmptyState(
                        onImport = { vm.dispatch(EditorEvent.OpenImportDialog) },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.entries, key = { it.id }) { entry ->
                            ReorderableItem(reorderState, key = entry.id) { isDragging ->
                                LaunchedEffect(isDragging) {
                                    if (isDragging) {
                                        haptics.thud()
                                    }
                                }

                                Box {
                                    FeldEntryCard(
                                        speakerName = entry.speaker.name,
                                        speakerColor = Color(entry.speaker.colorArgb),
                                        text = entry.text,
                                        seed = entry.id.hashCode(),
                                        isDragging = isDragging,
                                        onClick = { vm.dispatch(EditorEvent.RequestEditEntry(entry)) },
                                        onMore = { haptics.tick(); menuEntryId = entry.id },
                                        dragHandle = {
                                            Box(
                                                Modifier
                                                    .longPressDraggableHandle()
                                                    .size(32.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                FeldIcon(
                                                    FeldIconId.Drag,
                                                    tint = colorScheme.onSurface.copy(alpha = 0.5f),
                                                    size = 20.dp,
                                                )
                                            }
                                        },
                                    )
                                    // TODO: Change to FeldMenu
                                    DropdownMenu(
                                        expanded = menuEntryId == entry.id,
                                        onDismissRequest = { menuEntryId = null },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = {
                                                menuEntryId = null
                                                vm.dispatch(EditorEvent.RequestEditEntry(entry))
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                menuEntryId = null
                                                haptics.thud()
                                                eggs?.report("entry.delete", entry.speaker.name)
                                                vm.dispatch(EditorEvent.DeleteEntry(entry.id))
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                FeldFab(
                    onClick = { haptics.tick(); vm.dispatch(EditorEvent.RequestAddEntry) },
                    onLongClick = {
                        haptics.thud()
                        eggs?.report("fab.longpress")
                        vm.dispatch(EditorEvent.AddEntryWithRandomSpeaker)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    contentDescription = "Add dialogue entry",
                )
            }

//        FeldFooterBand(sceneName = state.sceneName)
        }
    }

    if (state.isSpeakerPickerOpen) {
        SpeakerPickerDialog(
            speakers = state.allSpeakers,
            onSpeakerSelected = { vm.dispatch(EditorEvent.SelectSpeakerForNewEntry(it)) },
            onCreateCustom = { vm.dispatch(EditorEvent.RequestCustomSpeaker) },
            onEditCustom = { vm.dispatch(EditorEvent.RequestEditSpeaker(it)) },
            onDismiss = { vm.dispatch(EditorEvent.CloseSpeakerPicker) },
        )
    }

    if (state.isImportDialogOpen) {
        BulkImportDialog(
            text = state.importText,
            preview = state.importPreview,
            onText = { vm.dispatch(EditorEvent.ImportTextChanged(it)) },
            onConfirm = { haptics.confirm(); vm.dispatch(EditorEvent.ConfirmImport) },
            onDismiss = { vm.dispatch(EditorEvent.CloseImportDialog) },
        )
    }

    if (state.isCustomSpeakerDialogOpen) {
        key(state.editingSpeaker?.id ?: "create") {
            CustomSpeakerDialog(
                speaker = state.editingSpeaker,
                onConfirm = { speaker ->
                    if (state.editingSpeaker != null)
                        vm.dispatch(EditorEvent.ConfirmEditSpeaker(speaker))
                    else
                        vm.dispatch(EditorEvent.ConfirmCustomSpeaker(speaker))
                },
                onDismiss = { vm.dispatch(EditorEvent.DismissCustomSpeaker) },
            )
        }
    }

    if (state.isEntryEditOpen) {
        EntryEditSheet(
            entry = state.editingEntry,
            speaker = state.editingEntry?.speaker ?: state.pendingSpeaker,
            onSpeakerClick = { vm.dispatch(EditorEvent.RequestChangeSpeaker) },
            onEditSpeaker = { vm.dispatch(EditorEvent.RequestEditSpeaker(it)) },
            onConfirm = { text ->
                val editing = state.editingEntry
                if (editing != null) {
                    eggs?.report("entry.edit", editing.speaker.name)
                    vm.dispatch(EditorEvent.ConfirmEditEntry(editing.copy(text = text)))
                } else {
                    eggs?.report("entry.add", state.pendingSpeaker?.name)
                    vm.dispatch(EditorEvent.ConfirmNewEntry(text))
                }
            },
            onDismiss = { vm.dispatch(EditorEvent.DismissEntryEdit) },
        )
    }
}

@Composable
private fun EmptyState(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            "NO DIALOGUE ENTRIES YET.",
            style = typography.labelSmall,
            color = colorScheme.onSurface.copy(alpha = 0.6f),
        )
        FeldButton("Bulk paste", onClick = onImport)
        Spacer(Modifier.height(14.dp))
    }
}
