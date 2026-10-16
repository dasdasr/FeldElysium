package com.example.feldelysium.presentation.editor

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.BuildConfig
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
import com.example.feldelysium.designsystem.foundation.decal.DecalContext
import com.example.feldelysium.designsystem.foundation.decal.LocalFeldDecalContext
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.sound.rememberFeldSounds
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.EggScreen
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.editor.components.BulkImportDialog
import com.example.feldelysium.presentation.editor.components.CustomSpeakerDialog
import com.example.feldelysium.presentation.editor.components.EntryEditSheet
import com.example.feldelysium.presentation.editor.components.ManageTagsDialog
import com.example.feldelysium.presentation.editor.components.SceneManagerDialog
import com.example.feldelysium.presentation.editor.components.SpeakerPickerDialog
import com.example.feldelysium.presentation.shared.SceneViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@SuppressLint("ContextCastToActivity")
@Composable
fun EditorScreen(onNavigateToPlayback: () -> Unit, onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalContext.current as ComponentActivity
    val haptics = rememberFeldHaptics()
    val sounds = rememberFeldSounds()
    val toast = rememberFeldToastState()
    val eggs = LocalEasterEggs.current

    val sceneVm: SceneViewModel = viewModel(activity)
    val vm: EditorViewModel = viewModel(factory = EditorViewModel.factory(sceneVm))
    val state by vm.state.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { vm.dispatch(EditorEvent.ExportScene(activity.contentResolver.openOutputStream(it))) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { vm.dispatch(EditorEvent.ImportScene(activity.contentResolver.openInputStream(it))) }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { sceneVm.saveNow() }

    var menuEntryId by remember { mutableStateOf<String?>(null) }
    var showManageTags by remember { mutableStateOf(false) }

    val decalCtx = remember(state.sceneName, state.entries.size) {
        DecalContext(
            sceneName = state.sceneName,
            lineCount = state.entries.size,
            segmentFrom = 1,
            segmentTo = state.entries.size.coerceAtLeast(1),
            appVersion = BuildConfig.VERSION_NAME,
        )
    }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                EditorEffect.NavigateToPlayback -> onNavigateToPlayback()

                is EditorEffect.ShowSnackbar ->
                    toast.show(effect.message)

                EditorEffect.LaunchGalleryPicker -> {
                    /* TODO */
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

    CompositionLocalProvider(LocalFeldDecalContext provides decalCtx) {
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
                    onDiamondClick = { eggs?.report(EggEvent.HeaderDiamond, EggScreen.Settings) },
                    onLabelClick = { vm.dispatch(EditorEvent.OpenSceneManager) },
                    onTitleCommit = { sounds.save(); vm.dispatch(EditorEvent.SaveScene) },
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
                            onClick = {
                                haptics.confirm()
                                vm.dispatch(EditorEvent.NavigateToPlayback)
                            },
                        ),
                        FeldToolbarItem(
                            FeldIconId.Settings,
                            "Settings",
                            onClick = onNavigateToSettings,
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
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 96.dp,
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
                                            ambient = 0.4f,
                                            seed = entry.id.hashCode(),
                                            isDragging = isDragging,
                                            dragModifier = Modifier.longPressDraggableHandle(),
                                            onClick = {
                                                vm.dispatch(
                                                    EditorEvent.RequestEditEntry(entry),
                                                )
                                            },
                                            onMore = {
                                                haptics.tick()
                                                menuEntryId = entry.id
                                            },
                                        )
                                        FeldMenu(
                                            expanded = menuEntryId == entry.id,
                                            onDismiss = { menuEntryId = null },
                                            seed = entry.id.hashCode(),
                                            items = listOf(
                                                FeldMenuItem("Edit") {
                                                    vm.dispatch(
                                                        EditorEvent.RequestEditEntry(
                                                            entry,
                                                        ),
                                                    )
                                                },
                                                FeldMenuItem("Delete", destructive = true) {
                                                    haptics.thud()
                                                    eggs?.report(
                                                        EggEvent.EntryDelete,
                                                        entry.speaker.name,
                                                    )
                                                    vm.dispatch(
                                                        EditorEvent.DeleteEntry(
                                                            entry.id,
                                                        ),
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
                        onClick = {
                            haptics.tick()
                            vm.dispatch(EditorEvent.RequestAddEntry)
                        },
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
                    FeldToastHost(toast, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }

    if (state.isSpeakerPickerOpen) {
        SpeakerPickerDialog(
            speakers = state.allSpeakers,
            tags = state.tags,
            onSpeakerSelected = {
                vm.dispatch(EditorEvent.SelectSpeakerForNewEntry(it))
            },
            onCreateCustom = {
                vm.dispatch(EditorEvent.RequestCustomSpeaker)
            },
            onEditCustom = {
                vm.dispatch(EditorEvent.RequestEditSpeaker(it))
            },
            onDismiss = {
                vm.dispatch(EditorEvent.CloseSpeakerPicker)
            },
        )
    }

    if (state.isImportDialogOpen) {
        BulkImportDialog(
            text = state.importText,
            preview = state.importPreview,
            onText = { vm.dispatch(EditorEvent.ImportTextChanged(it)) },
            onConfirm = {
                haptics.confirm()
                vm.dispatch(EditorEvent.ConfirmImport)
            },
            onDismiss = { vm.dispatch(EditorEvent.CloseImportDialog) },
        )
    }

    if (state.isCustomSpeakerDialogOpen) {
        key(state.editingSpeaker?.id ?: "create") {
            CustomSpeakerDialog(
                speaker = state.editingSpeaker,
                existingTypes = state.tags,
                onManageTags = { showManageTags = true },
                onDelete = state.editingSpeaker?.takeIf { !it.isBundled }?.let { s ->
                    {
                        vm.dispatch(EditorEvent.DeleteSpeaker(s.id))
                        vm.dispatch(EditorEvent.DismissCustomSpeaker)
                    }
                },
                onConfirm = { speaker ->
                    if (state.editingSpeaker != null) {
                        vm.dispatch(EditorEvent.ConfirmEditSpeaker(speaker))
                    } else {
                        vm.dispatch(EditorEvent.ConfirmCustomSpeaker(speaker))
                    }
                },
                onDismiss = { vm.dispatch(EditorEvent.DismissCustomSpeaker) },
            )
        }
    }

    if (showManageTags) {
        ManageTagsDialog(
            tags = state.tags,
            speakers = state.allSpeakers,
            onSave = { vm.dispatch(EditorEvent.SaveTag(it)) },
            onDelete = { vm.dispatch(EditorEvent.DeleteTag(it)) },
            onDismiss = { showManageTags = false },
        )
    }

    if (state.isSceneManagerOpen) {
        SceneManagerDialog(
            scenes = state.scenes,
            currentId = state.sceneId,
            onNew = { vm.dispatch(EditorEvent.NewScene) },
            onOpen = { vm.dispatch(EditorEvent.OpenScene(it)) },
            onDelete = { vm.dispatch(EditorEvent.DeleteScene(it)) },
            onExport = { exportLauncher.launch("${state.sceneName.ifBlank { "scene" }}.json") },
            onImport = { importLauncher.launch(arrayOf("*/*")) },
            onClear = { vm.dispatch(EditorEvent.ClearScene) },
            onDismiss = { vm.dispatch(EditorEvent.CloseSceneManager) },
        )
    }

    if (state.isEntryEditOpen) {
        EntryEditSheet(
            entry = state.editingEntry,
            speaker = state.editingEntry?.speaker ?: state.pendingSpeaker,
            onSpeakerClick = { vm.dispatch(EditorEvent.RequestChangeSpeaker) },
            onEditSpeaker = { vm.dispatch(EditorEvent.RequestEditSpeaker(it)) },
            onConfirm = { text, check, damage ->
                val editing = state.editingEntry

                if (editing != null) {
                    eggs?.report(EggEvent.EntryEdit, editing.speaker.name)
                    vm.dispatch(
                        EditorEvent.ConfirmEditEntry(
                            editing.copy(text = text, check = check, damage = damage),
                        ),
                    )
                } else {
                    eggs?.report(EggEvent.EntryAdd, state.pendingSpeaker?.name)
                    vm.dispatch(EditorEvent.ConfirmNewEntry(text, check, damage))
                }
            },
            onDismiss = { vm.dispatch(EditorEvent.DismissEntryEdit) },
        )
    }
}

@Composable
private fun EmptyState(onImport: () -> Unit, modifier: Modifier = Modifier) {
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
