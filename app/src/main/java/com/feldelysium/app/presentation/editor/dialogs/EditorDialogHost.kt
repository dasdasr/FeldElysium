
package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.app.eggs.EggEvent
import com.feldelysium.app.eggs.ui.LocalEasterEggs
import com.feldelysium.app.presentation.editor.EditorDialog
import com.feldelysium.app.presentation.editor.EditorEvent
import com.feldelysium.app.presentation.editor.EditorUiState
import com.feldelysium.app.presentation.editor.EditorViewModel
import com.feldelysium.app.presentation.editor.caption
import com.feldelysium.app.presentation.editor.entry.EntryEditSheet
import com.feldelysium.app.presentation.shared.dialogs.ManageTagsDialog

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
                        vm.dispatch(EditorEvent.AddEntry(speaker.id, text, check, damage, replies))
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
                        vm.dispatch(
                            EditorEvent.UpdateEntry(
                                entry.copy(
                                    speaker = speaker,
                                    text = text,
                                    check = check,
                                    damage = damage,
                                    replies = replies,
                                ),
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
                            vm.dispatch(EditorEvent.DeleteSpeaker(s.id))
                            onDialogChange(d.back)
                        }
                    },
                    onConfirm = {
                        vm.dispatch(EditorEvent.SaveSpeaker(it))
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
                    if (vm.dispatch(EditorEvent.ImportLines(text))) close()
                },
                onDismiss = close,
            )
        }

        EditorDialog.SceneManager -> {
            LaunchedEffect(Unit) { vm.dispatch(EditorEvent.RefreshScenes) }
            SceneManagerDialog(
                scenes = state.scenes,
                currentId = state.sceneId,
                onNew = {
                    vm.dispatch(EditorEvent.NewScene)
                    close()
                },
                onOpen = {
                    vm.dispatch(EditorEvent.OpenScene(it))
                    close()
                },
                onDelete = { vm.dispatch(EditorEvent.DeleteScene(it)) },
                onClearScene = { vm.dispatch(EditorEvent.ClearSceneById(it)) },
                onExport = onExportScene,
                onImport = onImportScene,
                onClear = { vm.dispatch(EditorEvent.ClearScene) },
                onDismiss = close,
                onRename = { id, title -> vm.dispatch(EditorEvent.RenameSceneById(id, title)) },
            )
        }
    }

    if (showManageTags) {
        ManageTagsDialog(
            tags = state.tags,
            speakers = state.allSpeakers,
            onSave = { vm.dispatch(EditorEvent.SaveTag(it)) },
            onDelete = { vm.dispatch(EditorEvent.DeleteTag(it.id)) },
            onDismiss = { showManageTags = false },
        )
    }
}
