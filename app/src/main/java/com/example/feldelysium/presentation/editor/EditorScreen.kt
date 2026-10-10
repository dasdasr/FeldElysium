package com.example.feldelysium.presentation.editor

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.button.FeldFab
import com.example.feldelysium.designsystem.components.card.FeldEntryCard
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldMenu
import com.example.feldelysium.designsystem.components.input.FeldMenuItem
import com.example.feldelysium.designsystem.components.input.FeldTapOutsideDismiss
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.navigation.FeldToolbar
import com.example.feldelysium.designsystem.components.navigation.FeldToolbarItem
import com.example.feldelysium.designsystem.components.toast.FeldToastHost
import com.example.feldelysium.designsystem.components.toast.rememberFeldToastState
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.EggScreen
import com.example.feldelysium.eggs.ui.LocalEasterEggs
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
    val toast = rememberFeldToastState()
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
                    toast.show(effect.message)

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
                .background(colorScheme.background)
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
                onDiamondClick = { eggs?.report(EggEvent.HeaderDiamond, EggScreen.Settings) }
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
                            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        itemsIndexed(state.entries, key = { _, e -> e.id }) { index, entry ->
                            ReorderableItem(reorderState, key = entry.id) { isDragging ->
                                LaunchedEffect(isDragging) {
                                    if (isDragging) haptics.thud()
                                }

                                Box {
                                    FeldEntryCard(
                                        speakerName = entry.speaker.name,
                                        speakerColor = Color(entry.speaker.colorArgb),
                                        text = entry.text,
                                        number = index + 1,
                                        seed = entry.id.hashCode(),
                                        isDragging = isDragging,
                                        dragModifier = Modifier.longPressDraggableHandle(),
                                        onClick = {
                                            vm.dispatch(
                                                EditorEvent.RequestEditEntry(entry)
                                            )
                                        },
                                        onMore = { haptics.tick(); menuEntryId = entry.id },
                                    )
                                    FeldMenu(
                                        expanded = menuEntryId == entry.id,
                                        onDismiss = { menuEntryId = null },
                                        seed = entry.id.hashCode(),
                                        items = listOf(
                                            FeldMenuItem("Edit") {
                                                vm.dispatch(
                                                    EditorEvent.RequestEditEntry(
                                                        entry
                                                    )
                                                )
                                            },
                                            FeldMenuItem("Delete", destructive = true) {
                                                haptics.thud()
                                                eggs?.report(
                                                    EggEvent.EntryDelete,
                                                    entry.speaker.name
                                                )
                                                vm.dispatch(
                                                    EditorEvent.DeleteEntry(
                                                        entry.id
                                                    )
                                                )
                                            },
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                FeldFab(
                    onClick = { haptics.tick(); vm.dispatch(EditorEvent.RequestAddEntry) },
                    onLongClick = {
                        haptics.thud()
                        eggs?.report(EggEvent.FabLongPress)
                        vm.dispatch(EditorEvent.AddEntryWithRandomSpeaker)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    contentDescription = "Add dialogue entry",
                )
                FeldToastHost(toast, Modifier.align(Alignment.TopCenter))
            }
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
                    eggs?.report(EggEvent.EntryEdit, editing.speaker.name)
                    vm.dispatch(EditorEvent.ConfirmEditEntry(editing.copy(text = text)))
                } else {
                    eggs?.report(EggEvent.EntryAdd, state.pendingSpeaker?.name)
                    vm.dispatch(EditorEvent.ConfirmNewEntry(text))
                }
            },
            onDismiss = { vm.dispatch(EditorEvent.DismissEntryEdit) },
        )
    }
}

@Composable
private fun EmptyState(
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.feldClickable(onClick = onImport),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(R.drawable.empty_editor_text),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .markerFilmMaterial(
                        FeldMarker.Chalk,
                        seed = 7,
                    )
                    .padding(
                        start = 28.dp,
                        top = 28.dp,
                        end = 24.dp,
                        bottom = 6.dp,
                    ),
            )

            Text(
                text = "Paste a script or\nadd a line.",
                style = MaterialTheme
                    .typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.62f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )

            Spacer(Modifier.height(78.dp))
        }
    }
}
