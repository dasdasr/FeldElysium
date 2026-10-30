package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.app.domain.model.SceneInfo

@Composable
fun SceneManagerDialog(
    scenes: List<SceneInfo>,
    currentId: String,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onRename: (id: String, title: String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit,
    onClearScene: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var menuId by remember { mutableStateOf<String?>(null) }
    var renameId by remember { mutableStateOf<String?>(null) }
    val haptics = rememberFeldHaptics()
    val currentLines = scenes.firstOrNull { it.id == currentId }?.lines ?: 0
    val ordered = remember(scenes, currentId) { scenes.sortedBy { if (it.id == currentId) 0 else 1 } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
            ManagerTopBar(count = scenes.size, onBack = onDismiss)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "title", span = { GridItemSpan(maxLineSpan) }) { ManagerTitle() }
                itemsIndexed(
                    ordered,
                    key = { _, s -> s.id },
                    span = { _, s -> GridItemSpan(if (s.id == currentId) maxLineSpan else 1) },
                ) { index, scene ->
                    val isCurrent = scene.id == currentId
                    SceneFolder(
                        scene = scene,
                        index = index,
                        featured = isCurrent,
                        menuOpen = menuId == scene.id,
                        onClick = {
                            haptics.tick()
                            onOpen(scene.id)
                        },
                        onLongClick = {
                            haptics.thud()
                            menuId = scene.id
                        },
                        onDismissMenu = { menuId = null },
                        onRename = {
                            menuId = null
                            renameId = scene.id
                        },
                        onClear = {
                            menuId = null
                            if (isCurrent) onClear() else onClearScene(scene.id)
                        },
                        onDelete = {
                            menuId = null
                            onDelete(scene.id)
                        },
                    )
                }
                item(key = "new-scene", span = { GridItemSpan(maxLineSpan) }) {
                    NewSceneCard(onClick = {
                        haptics.tick()
                        onNew()
                    })
                }
            }

            ActionBar(canClear = currentLines > 0, onClear = onClear, onImport = onImport, onExport = onExport)
        }
    }

    renameId?.let { id ->
        val scene = scenes.firstOrNull { it.id == id }
        if (scene == null) {
            renameId = null
        } else {
            RenameSceneDialog(
                initial = scene.title,
                onConfirm = {
                    onRename(id, it.take(60))
                    renameId = null
                },
                onDismiss = { renameId = null },
            )
        }
    }
}
