
package com.example.feldelysium.presentation.editor.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.presentation.editor.EditorDialog
import com.example.feldelysium.presentation.editor.EditorUiState
import com.example.feldelysium.presentation.editor.EditorViewModel
import com.example.feldelysium.presentation.editor.caption
import com.example.feldelysium.presentation.editor.entry.EntryEditSheet
import com.example.feldelysium.presentation.shared.dialogs.ManageTagsDialog

@Composable
internal fun EditorDialogHost(
    dialog: EditorDialog,
    onDialogChange: (EditorDialog) -> Unit,
    state: EditorUiState,
    vm: EditorViewModel,
    onExportScene: () -> Unit,
    onImportScene: () -> Unit,
) {
    val context = LocalContext.current
    val haptics = rememberFeldHaptics()
    val eggs = LocalEasterEggs.current
    var showManageTags by remember { mutableStateOf(false) }

    val close = { onDialogChange(EditorDialog.None) }

    when (val d = dialog) {
        EditorDialog.None -> Unit

        is EditorDialog.NewEntry -> {
            val speaker = d.speakerId?.let(state::speaker)
            if (speaker == null) {
                SpeakerPickerDialog(
                    speakers = state.pickerSpeakers,
                    tags = state.tags,
                    onSpeakerSelected = { onDialogChange(EditorDialog.NewEntry(it.id)) },
                    onCreateCustom = { onDialogChange(EditorDialog.EditSpeaker(null, back = d)) },
                    onEditCustom = { onDialogChange(EditorDialog.EditSpeaker(it.id, back = d)) },
                    onDismiss = close,
                )
            } else {
                EntryEditSheet(
                    entry = null,
                    speaker = speaker,
                    onSpeakerClick = { onDialogChange(EditorDialog.NewEntry(null)) },
                    onEditSpeaker = { onDialogChange(EditorDialog.EditSpeaker(it.id, back = d)) },
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
                    onSpeakerSelected = {
                        onDialogChange(d.copy(speakerId = it.id, pickingSpeaker = false))
                    },
                    onCreateCustom = { onDialogChange(EditorDialog.EditSpeaker(null, back = d)) },
                    onEditCustom = { onDialogChange(EditorDialog.EditSpeaker(it.id, back = d)) },
                    onDismiss = {
                        if (speaker == null) close() else onDialogChange(d.copy(pickingSpeaker = false))
                    },
                )

                else -> EntryEditSheet(
                    entry = entry,
                    speaker = speaker,
                    onSpeakerClick = { onDialogChange(d.copy(pickingSpeaker = true)) },
                    onEditSpeaker = { onDialogChange(EditorDialog.EditSpeaker(it.id, back = d)) },
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
                            onDialogChange(d.back)
                        }
                    },
                    onConfirm = {
                        vm.saveSpeaker(it)
                        onDialogChange(d.back)
                    },
                    onDismiss = { onDialogChange(d.back) },
                )
            }
        }

        EditorDialog.ImportLines -> {
            var text by rememberSaveable { mutableStateOf("") }
            val preview = remember(text, state.allSpeakers) { vm.previewImport(text) }

            BulkImportDialog(
                text = text,
                previewCaption = preview?.caption(context),
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
                onClearScene = vm::clearSceneById,
                onExport = onExportScene,
                onImport = onImportScene,
                onClear = vm::clearScene,
                onDismiss = close,
                onRename = { id, title -> vm.renameSceneById(id, title) },
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
