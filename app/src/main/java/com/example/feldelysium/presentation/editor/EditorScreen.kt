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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.BuildConfig
import com.example.feldelysium.FeldApp
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
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.EggScreen
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.editor.components.BulkImportDialog
import com.example.feldelysium.presentation.editor.components.CustomSpeakerDialog
import com.example.feldelysium.presentation.editor.components.EntryEditSheet
import com.example.feldelysium.presentation.editor.components.ManageTagsDialog
import com.example.feldelysium.presentation.editor.components.SceneManagerDialog
import com.example.feldelysium.presentation.editor.components.SpeakerPickerDialog
import com.example.feldelysium.presentation.settings.SettingsViewModel
import com.example.feldelysium.presentation.shared.FeldError
import com.example.feldelysium.presentation.shared.toastText
import com.example.feldelysium.record.LocalScreenRecordRequest
import com.example.feldelysium.record.RecordResult
import com.example.feldelysium.record.ScreenRecordService
import com.example.feldelysium.theme.fontDir
import com.example.feldelysium.theme.rememberPlaybackFontFamily
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@SuppressLint("ContextCastToActivity")
@Composable
fun EditorScreen(onNavigateToPlayback: () -> Unit, onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = (context.applicationContext as FeldApp).container

    val activity = context as ComponentActivity
    val settingsVm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container, activity.fontDir()),
    )
    val settings by settingsVm.state.collectAsStateWithLifecycle()
    val fontFamily = rememberPlaybackFontFamily(settings)
    val requestRecord = LocalScreenRecordRequest.current

    val vm: EditorViewModel = viewModel(
        factory = EditorViewModel.factory(container),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val haptics = rememberFeldHaptics()
    val sounds = rememberFeldSounds()
    val toast = rememberFeldToastState()
    val eggs = LocalEasterEggs.current

    var dialog by rememberSaveable(stateSaver = EditorDialogSaver) {
        mutableStateOf<EditorDialog>(EditorDialog.None)
    }
    var menuEntryId by remember { mutableStateOf<String?>(null) }
    var showManageTags by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { vm.exportScene(context.contentResolver.openOutputStream(it)) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            vm.importScene(context.contentResolver.openInputStream(it))
            dialog = EditorDialog.None
        }
    }

    LaunchedEffect(Unit) { vm.messages.collect { toast.show(it) } }

    val recordResult by ScreenRecordService.result.collectAsStateWithLifecycle()
    LaunchedEffect(recordResult) {
        val result = recordResult ?: return@LaunchedEffect
        if (result is RecordResult.Saved) {
            haptics.confirm()
            sounds.save()
        }
        toast.show(result.toastText(askedForSound = settings.exportAudio))
        ScreenRecordService.consumeResult()
    }

    val decalCtx = remember(state.sceneName, state.entries.size) {
        DecalContext(
            sceneName = state.sceneName,
            lineCount = state.entries.size,
            segmentFrom = 1,
            segmentTo = state.entries.size.coerceAtLeast(1),
            appVersion = BuildConfig.VERSION_NAME,
        )
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        haptics.handleMove()
        vm.reorderEntries(state.entries.toMutableList().apply { add(to.index, removeAt(from.index)) })
    }

    fun deleteEntry(entry: DialogueEntry) {
        haptics.thud()
        eggs?.report(EggEvent.EntryDelete, entry.speaker.name)
        vm.deleteEntry(entry.id)
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
                    onTitleChange = { vm.renameScene(it.take(60)) },
                    onDiamondClick = { eggs?.report(EggEvent.HeaderDiamond, EggScreen.Settings) },
                    onLabelClick = { dialog = EditorDialog.SceneManager },
                    onTitleCommit = {
                        sounds.save()
                        vm.saveScene()
                    },
                )

                FeldToolbar(
                    items = buildList {
                        add(
                            FeldToolbarItem(
                                FeldIconId.BulkImport,
                                "Paste",
                            ) {
                                dialog = EditorDialog.ImportLines
                            },
                        )

                        add(
                            FeldToolbarItem(
                                icon = FeldIconId.Play,
                                label = "Playback",
                                enabled = state.entries.isNotEmpty(),
                                onClick = {
                                    haptics.confirm()
                                    if (settings.exportEnabled) {
                                        requestRecord(
                                            settings.exportAudio,
                                            { onNavigateToPlayback() },
                                            { toast.show(FeldError.RecordDenied.toastText(), durationMillis = 4000L) },
                                        )
                                    } else {
                                        onNavigateToPlayback()
                                    }
                                },
                            ),
                        )
                        add(
                            FeldToolbarItem(
                                FeldIconId.Settings,
                                "Settings",
                                onClick = onNavigateToSettings,
                            ),
                        )
                    },
                )

                Box(Modifier.weight(1f)) {
                    if (state.entries.isEmpty()) {
                        EmptyState(
                            onImport = { dialog = EditorDialog.ImportLines },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            itemsIndexed(state.entries, key = { _, e -> e.id }) { index, entry ->
                                ReorderableItem(reorderState, key = entry.id) { isDragging ->
                                    LaunchedEffect(isDragging) { if (isDragging) haptics.thud() }

                                    val edit = { dialog = EditorDialog.EditEntry(entry.id, entry.speaker.id) }
                                    val selected = menuEntryId == entry.id

                                    Box {
                                        FeldEntryCard(
                                            speakerName = entry.speaker.name,
                                            speakerColor = Color(entry.speaker.colorArgb),
                                            text = entry.text,
                                            number = index + 1,
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
                                            onSwipeDelete = { deleteEntry(entry) },
                                        )
                                        Box(Modifier.align(Alignment.Center)) {
                                            FeldMenu(
                                                expanded = selected,
                                                onDismiss = { menuEntryId = null },
                                                seed = entry.id.hashCode(),
                                                offset = DpOffset(0.dp, 0.dp),
                                                items = listOf(
                                                    FeldMenuItem("Edit", FeldIconId.Edit, onClick = edit),
                                                    FeldMenuItem(
                                                        "Delete",
                                                        FeldIconId.Delete,
                                                        destructive = true,
                                                    ) { deleteEntry(entry) },
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    FeldFab(
                        onClick = {
                            haptics.tick()
                            dialog = EditorDialog.NewEntry()
                        },
                        onLongClick = {
                            haptics.thud()
                            eggs?.report(EggEvent.FabLongPress)
                            dialog = EditorDialog.NewEntry(state.pickerSpeakers.randomOrNull()?.id)
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

    val close = { dialog = EditorDialog.None }

    when (val d = dialog) {
        EditorDialog.None -> Unit

        is EditorDialog.NewEntry -> {
            val speaker = d.speakerId?.let(state::speaker)
            if (speaker == null) {
                SpeakerPickerDialog(
                    speakers = state.pickerSpeakers,
                    tags = state.tags,
                    onSpeakerSelected = { dialog = EditorDialog.NewEntry(it.id) },
                    onCreateCustom = { dialog = EditorDialog.EditSpeaker(null, back = d) },
                    onEditCustom = { dialog = EditorDialog.EditSpeaker(it.id, back = d) },
                    onDismiss = close,
                )
            } else {
                EntryEditSheet(
                    entry = null,
                    speaker = speaker,
                    onSpeakerClick = { dialog = EditorDialog.NewEntry(null) },
                    onEditSpeaker = { dialog = EditorDialog.EditSpeaker(it.id, back = d) },
                    onConfirm = { text, check, damage, replies ->
                        eggs?.report(EggEvent.EntryAdd, speaker.name)
                        vm.addEntry(speaker.id, text, check, damage, replies)
                        close()
                    },
                    onDismiss = close,
                )
            }
        }

        is EditorDialog.EditEntry -> {
            val entry = state.entry(d.entryId)
            val speaker = state.speaker(d.speakerId)
            when {
                entry == null -> close()

                speaker == null || d.pickingSpeaker -> SpeakerPickerDialog(
                    speakers = state.pickerSpeakers,
                    tags = state.tags,
                    onSpeakerSelected = { dialog = d.copy(speakerId = it.id, pickingSpeaker = false) },
                    onCreateCustom = { dialog = EditorDialog.EditSpeaker(null, back = d) },
                    onEditCustom = { dialog = EditorDialog.EditSpeaker(it.id, back = d) },
                    onDismiss = { if (speaker == null) close() else dialog = d.copy(pickingSpeaker = false) },
                )

                else -> EntryEditSheet(
                    entry = entry,
                    speaker = speaker,
                    onSpeakerClick = { dialog = d.copy(pickingSpeaker = true) },
                    onEditSpeaker = { dialog = EditorDialog.EditSpeaker(it.id, back = d) },
                    onConfirm = { text, check, damage, replies ->
                        eggs?.report(EggEvent.EntryEdit, entry.speaker.name)
                        vm.updateEntry(
                            entry.copy(
                                speaker = speaker,
                                text = text,
                                check = check,
                                damage = damage,
                                replies = replies,
                            ),
                        )
                        close()
                    },
                    onDismiss = close,
                )
            }
        }

        is EditorDialog.EditSpeaker -> {
            val speaker = d.speakerId?.let(state::speaker)
            key(d.speakerId ?: "create") {
                CustomSpeakerDialog(
                    speaker = speaker,
                    existingTypes = state.tags,
                    onManageTags = { showManageTags = true },
                    onDelete = speaker?.takeIf { !it.isBundled }?.let { s ->
                        {
                            vm.deleteSpeaker(s.id)
                            dialog = d.back
                        }
                    },
                    onConfirm = {
                        vm.saveSpeaker(it)
                        dialog = d.back
                    },
                    onDismiss = { dialog = d.back },
                )
            }
        }

        EditorDialog.ImportLines -> {
            var text by rememberSaveable { mutableStateOf("") }
            val preview = remember(text, state.allSpeakers) { vm.previewImport(text) }
            BulkImportDialog(
                text = text,
                preview = preview,
                onText = { text = it },
                onConfirm = {
                    haptics.confirm()
                    if (vm.importLines(text)) close()
                },
                onDismiss = close,
            )
        }

        EditorDialog.SceneManager -> {
            LaunchedEffect(Unit) { vm.refreshScenes() }
            SceneManagerDialog(
                scenes = state.scenes,
                currentId = state.sceneId,
                onNew = {
                    vm.newScene()
                    close()
                },
                onOpen = {
                    vm.openScene(it)
                    close()
                },
                onDelete = vm::deleteScene,
                onExport = { exportLauncher.launch("${state.sceneName.ifBlank { "scene" }}.json") },
                onImport = { importLauncher.launch(arrayOf("*/*")) },
                onClear = vm::clearScene,
                onDismiss = close,
            )
        }
    }

    if (showManageTags) {
        ManageTagsDialog(
            tags = state.tags,
            speakers = state.allSpeakers,
            onSave = vm::saveTag,
            onDelete = vm::deleteTag,
            onDismiss = { showManageTags = false },
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
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.62f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )

            Spacer(Modifier.height(78.dp))
        }
    }
}

private fun RecordResult.toastText(askedForSound: Boolean): String = when (this) {
    is RecordResult.Saved -> buildString {
        append("VIDEO SAVED\n")
        append(if (uri != null) "Gallery › FeldElysium" else "App folder › Movies")
        if (askedForSound && !hasAudio) append(" · no sound")
    }

    RecordResult.Failed -> FeldError.RecordFailed.toastText()
}
