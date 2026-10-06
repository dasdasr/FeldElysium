package com.example.feldelysium.presentation.editor

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.tooling.preview.Preview
import com.example.feldelysium.presentation.editor.components.BulkImportDialog
import com.example.feldelysium.presentation.editor.components.CustomSpeakerDialog
import com.example.feldelysium.presentation.editor.components.DialogueEntryCard
import com.example.feldelysium.presentation.editor.components.EntryEditSheet
import com.example.feldelysium.presentation.editor.components.SpeakerPickerDialog
import com.example.feldelysium.presentation.shared.SceneViewModel

@Preview
@Composable
private fun EditorScreenPreview() {
    EditorScreen(
        onNavigateToPlayback = {},
    )
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateToPlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as ComponentActivity
    val sceneVm: SceneViewModel = viewModel(activity)
    val vm: EditorViewModel = viewModel(factory = EditorViewModel.factory(sceneVm))

    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                EditorEffect.NavigateToPlayback ->
                    onNavigateToPlayback()

                is EditorEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)

                is EditorEffect.ShowImportWarning ->
                    snackbarHostState.showSnackbar(
                        "Imported with warnings. Skipped: ${effect.skipped.joinToString()}"
                    )

                EditorEffect.LaunchGalleryPicker -> { /* TODO: launch ActivityResultContract */
                }
            }
        }
    }

    // ── Reorderable list state ────────────────────────────────────────────────
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val reordered = state.entries.toMutableList().apply { add(to.index, removeAt(from.index)) }
        vm.dispatch(EditorEvent.ReorderEntries(reordered))
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.sceneName) },
                actions = {
                    IconButton(onClick = { vm.dispatch(EditorEvent.OpenImportDialog) }) {
                        Icon(Icons.Default.Upload, "Bulk Import")
                    }
                    IconButton(
                        onClick = { vm.dispatch(EditorEvent.NavigateToPlayback) },
                        enabled = state.entries.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.dispatch(EditorEvent.RequestAddEntry) }) {
                Icon(Icons.Default.Add, "Add Dialogue Entry")
            }
        },
        modifier = modifier
    ) { padding ->
        if (state.entries.isEmpty()) {
            EmptyStatePrompt(
                onImport = { vm.dispatch(EditorEvent.OpenImportDialog) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    ReorderableItem(reorderState, key = entry.id) { isDragging ->
                        DialogueEntryCard(
                            entry = entry,
                            isDragging = isDragging,
                            onEdit = { vm.dispatch(EditorEvent.RequestEditEntry(entry)) },
                            onDelete = { vm.dispatch(EditorEvent.DeleteEntry(entry.id)) },
                            dragHandle = { /* pass the reorderable drag handle modifier here */ }
                        )
                    }
                }
            }
        }
    }

    if (state.isSpeakerPickerOpen) {
        SpeakerPickerDialog(
            speakers = state.allSpeakers,
            onSpeakerSelected = { vm.dispatch(EditorEvent.SelectSpeakerForNewEntry(it)) },
            onCreateCustom = { vm.dispatch(EditorEvent.RequestCustomSpeaker) },
            onDismiss = { vm.dispatch(EditorEvent.CloseSpeakerPicker) }
        )
    }

    if (state.isImportDialogOpen) {
        BulkImportDialog(
            text = state.importText,
            error = state.importError,
            onText = { vm.dispatch(EditorEvent.ImportTextChanged(it)) },
            onConfirm = { vm.dispatch(EditorEvent.ConfirmImport) },
            onDismiss = { vm.dispatch(EditorEvent.CloseImportDialog) }
        )
    }

    if (state.isCustomSpeakerDialogOpen) {
        CustomSpeakerDialog(
            onConfirm = { vm.dispatch(EditorEvent.ConfirmCustomSpeaker(it)) },
            onDismiss = { vm.dispatch(EditorEvent.DismissCustomSpeaker) }
        )
    }

    if (state.isEntryEditOpen) {
        EntryEditSheet(
            entry = state.editingEntry,
            speaker = state.editingEntry?.speaker ?: state.pendingSpeaker,
            onConfirm = { text ->
                if (state.editingEntry != null)
                    vm.dispatch(EditorEvent.ConfirmEditEntry(state.editingEntry!!.copy(text = text)))
                else
                    vm.dispatch(EditorEvent.ConfirmNewEntry(text))
            },
            onDismiss = { vm.dispatch(EditorEvent.DismissEntryEdit) }
        )
    }
}

@Composable
private fun EmptyStatePrompt(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No dialogue entries yet.", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onImport) { Text("Bulk Import") }
        }
    }
}
