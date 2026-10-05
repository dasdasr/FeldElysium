package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private const val ANCHOR_BOTTOM_INSET_FRACTION = 0.4f

@Preview
@Composable
private fun DialogueHistoryListPreview() {
    DialogueHistoryList(
        entries = listOf(
            DialogueEntry(
                speaker = PredefinedSpeakers.STEBAN,
                text = "Go-go gyro"
            ),
            DialogueEntry(
                speaker = PredefinedSpeakers.STEBAN,
                text = "Go-go gyro"
            ),
            DialogueEntry(
                speaker = PredefinedSpeakers.STEBAN,
                text = "Go-go gyro"
            )
        ),
        listState = LazyListState()
    )
}

@Composable
fun DialogueHistoryList(
    entries: List<DialogueEntry>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val anchorInset = maxHeight * ANCHOR_BOTTOM_INSET_FRACTION
        val reversedEntries = entries.asReversed()

        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = anchorInset
            )
        ) {
            itemsIndexed(
                items = reversedEntries,
                key = { _, entry -> entry.id }
            ) { reversedIndex, entry ->
                DialogueEntryItem(
                    entry = entry,
                    alpha = alphaForDistance(reversedIndex),
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

private fun alphaForDistance(distanceFromLatest: Int): Float = when {
    distanceFromLatest == 0 -> 1.00f
    distanceFromLatest == 1 -> 0.80f
    distanceFromLatest == 2 -> 0.58f
    distanceFromLatest == 3 -> 0.38f
    distanceFromLatest == 4 -> 0.22f
    else -> 0.12f
}