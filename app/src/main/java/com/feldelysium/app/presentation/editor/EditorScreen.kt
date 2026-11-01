package com.feldelysium.app.presentation.editor

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feldelysium.app.BuildConfig
import com.feldelysium.app.FeldApp
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.eggs.EggEvent
import com.feldelysium.app.eggs.EggScreen
import com.feldelysium.app.eggs.ui.LocalEasterEggs
import com.feldelysium.app.presentation.editor.dialogs.EditorDialogHost
import com.feldelysium.app.presentation.editor.sections.EditorToolbar
import com.feldelysium.app.presentation.editor.sections.EmptyState
import com.feldelysium.app.presentation.editor.sections.EntryList
import com.feldelysium.app.presentation.settings.SettingsViewModel
import com.feldelysium.app.presentation.shared.FeldError
import com.feldelysium.app.presentation.shared.toastText
import com.feldelysium.app.presentation.theme.fontDir
import com.feldelysium.app.presentation.theme.rememberPlaybackFontFamily
import com.feldelysium.app.record.LocalScreenRecordRequest
import com.feldelysium.app.record.RecordResult
import com.feldelysium.app.record.ScreenRecordService
import com.feldelysium.designsystem.components.button.FeldFab
import com.feldelysium.designsystem.components.container.FeldToastHost
import com.feldelysium.designsystem.components.container.FeldToastState
import com.feldelysium.designsystem.components.container.rememberFeldToastState
import com.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.feldelysium.designsystem.foundation.decal.DecalContext
import com.feldelysium.designsystem.foundation.decal.LocalFeldDecalContext
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.sound.feldSounds

@SuppressLint("ContextCastToActivity")
@Composable
fun EditorScreen(onNavigateToPlayback: () -> Unit, onNavigateToSettings: () -> Unit) {
    val activity = LocalContext.current as ComponentActivity
    val container = (activity.application as FeldApp).container

    val settingsVm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container, activity.fontDir()),
    )
    val settings by settingsVm.state.collectAsStateWithLifecycle()
    val fontFamily = rememberPlaybackFontFamily(settings)
    val requestRecord = LocalScreenRecordRequest.current

    val vm: EditorViewModel = viewModel(factory = EditorViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()

    val haptics = rememberFeldHaptics()
    val sounds = feldSounds
    val toast = rememberFeldToastState()

    var dialog by rememberSaveable(stateSaver = EditorDialogSaver) {
        mutableStateOf<EditorDialog>(EditorDialog.None)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { target ->
            activity.contentResolver.openOutputStream(target)?.let {
                vm.dispatch(EditorEvent.ExportScene(it))
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            activity.contentResolver.openInputStream(it)?.let { stream ->
                vm.dispatch(EditorEvent.ImportScene(stream))
            }
            dialog = EditorDialog.None
        }
    }

    LaunchedEffect(Unit) { vm.messages.collect { toast.show(it.resolve(activity)) } }

    val recordResult by ScreenRecordService.result.collectAsStateWithLifecycle()
    LaunchedEffect(recordResult) {
        val result = recordResult ?: return@LaunchedEffect
        if (result is RecordResult.Saved) {
            haptics.confirm()
            sounds.save()
        }
        toast.show(result.toastText(activity, askedForSound = settings.exportAudio))
        ScreenRecordService.consumeResult()
    }

    EditorContent(
        state = state,
        fontFamily = fontFamily,
        toast = toast,
        onOpenSceneManager = { dialog = EditorDialog.SceneManager },
        onOpenImport = { dialog = EditorDialog.ImportLines },
        onPlayback = {
            haptics.confirm()
            if (settings.exportEnabled) {
                requestRecord(
                    settings.exportAudio,
                    onNavigateToPlayback,
                    {
                        toast.show(
                            FeldError.RecordDenied.toastText(activity),
                            durationMillis = 4000L,
                        )
                    },
                )
            } else {
                onNavigateToPlayback()
            }
        },
        onSettings = onNavigateToSettings,
        onAddEntry = { dialog = EditorDialog.NewEntry(it) },
        onEditEntry = { dialog = EditorDialog.EditEntry(it.id, it.speaker.id) },
        onDeleteEntry = { vm.dispatch(EditorEvent.DeleteEntry(it)) },
        onReorder = { vm.dispatch(EditorEvent.ReorderEntries(it)) },
    )

    EditorDialogHost(
        dialog = dialog,
        onDialogChange = { dialog = it },
        state = state,
        vm = vm,
        onExportScene = {
            exportLauncher.launch("${state.sceneName.ifBlank { "scene" }}.json")
        },
        onImportScene = { importLauncher.launch(arrayOf("*/*")) },
    )
}

private fun RecordResult.toastText(context: Context, askedForSound: Boolean): String = when (this) {
    is RecordResult.Saved -> context.getString(
        R.string.record_video_saved_message,
        context.getString(
            if (uri != null) R.string.record_gallery_location else R.string.record_app_folder_location,
        ),
        if (askedForSound && !hasAudio) context.getString(R.string.record_no_sound_suffix) else "",
    )

    RecordResult.Failed -> FeldError.RecordFailed.toastText(context)
}

@Composable
internal fun EditorContent(
    state: EditorUiState,
    fontFamily: FontFamily,
    toast: FeldToastState,
    onOpenSceneManager: () -> Unit,
    onOpenImport: () -> Unit,
    onPlayback: () -> Unit,
    onSettings: () -> Unit,
    onAddEntry: (speakerId: String?) -> Unit,
    onEditEntry: (DialogueEntry) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onReorder: (List<DialogueEntry>) -> Unit,
) {
    val haptics = rememberFeldHaptics()
    val sounds = feldSounds
    val eggs = LocalEasterEggs.current

    val decalContext = remember(state.sceneName, state.entries.size) {
        DecalContext(
            sceneName = state.sceneName,
            lineCount = state.entries.size,
            segmentFrom = 1,
            segmentTo = state.entries.size.coerceAtLeast(1),
            appVersion = BuildConfig.VERSION_NAME,
        )
    }

    val titleSize = remember(state.sceneName) { sceneTitleFontSize(state.sceneName.length) }
    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = titleSize,
        lineHeight = titleSize * 1.04f,
        letterSpacing = 1.sp,
    )

    CompositionLocalProvider(LocalFeldDecalContext provides decalContext) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            FeldScreenHeader(
                title = state.sceneName,
                eyebrow = stringResource(R.string.editor_eyebrow),
                metaStart = stringResource(R.string.editor_lines_count, state.entries.size),
                metaEnd = stringResource(R.string.editor_scene_meta),
                navIcon = null,
                titleSingleLine = true,
                titleStyle = titleStyle,
                onDiamondClick = { eggs?.report(EggEvent.HeaderDiamond, EggScreen.Settings) },
                onClick = onOpenSceneManager,
            )

            EditorToolbar(
                hasEntries = state.entries.isNotEmpty(),
                onPaste = onOpenImport,
                onPlayback = onPlayback,
                onSettings = onSettings,
            )

            Box(Modifier.weight(1f)) {
                if (state.entries.isEmpty()) {
                    EmptyState(onImport = onOpenImport, modifier = Modifier.fillMaxSize())
                } else {
                    EntryList(
                        entries = state.entries,
                        fontFamily = fontFamily,
                        onEdit = onEditEntry,
                        onDelete = onDeleteEntry,
                        onReorder = onReorder,
                    )
                }

                FeldFab(
                    onClick = {
                        haptics.tick()
                        onAddEntry(null)
                    },
                    onLongClick = {
                        haptics.thud()
                        eggs?.report(EggEvent.FabLongPress)
                        onAddEntry(state.pickerSpeakers.randomOrNull()?.id)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    contentDescription = stringResource(R.string.editor_add_entry),
                )

                FeldToastHost(toast, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

private fun sceneTitleFontSize(
    length: Int,
    max: Float = 52f,
    min: Float = 30f,
    shrinkFrom: Int = 12,
    shrinkTo: Int = 32,
): TextUnit {
    val t = ((length - shrinkFrom).toFloat() / (shrinkTo - shrinkFrom)).coerceIn(0f, 1f)
    return (max + (min - max) * t).sp
}
