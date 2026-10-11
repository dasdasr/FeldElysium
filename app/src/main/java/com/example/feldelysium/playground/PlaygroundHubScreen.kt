package com.example.feldelysium.playground

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldDistressedTitle
import com.example.feldelysium.designsystem.components.FeldDistressedTitles
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.display.FeldDiamond
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.theme.disco
import org.w3c.dom.Text
import java.time.format.TextStyle

data class PlaygroundEntry(
    val id: String,
    val title: String,
    val description: String,
    val content: @Composable () -> Unit,
)

@Composable
fun PlaygroundHubScreen(
    playgrounds: List<PlaygroundEntry>,
    onLaunchApp: () -> Unit,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    val selected = playgrounds.find { it.id == selectedId }

    if (selected != null) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Button(
                onClick = { selectedId = null },
                modifier = Modifier.padding(12.dp),
            ) {
                Text("← All playgrounds")
            }

            selected.content()
        }
        return
    }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FeldCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLaunchApp),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FeldHandDiamond(
                                color = MaterialTheme.colorScheme.primary,
                                seed = rememberSeed(),
                                size = 14.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            FeldDistressedTitle(
                                text = "Run application",
                                spec = FeldDistressedTitles.Large,
                            )
                        }
                        Text(
                            text = "Launch the regular app with AppNavGraph.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            item {
                Text(
                    text = "UI PLAYGROUNDS",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp),
                )
            }

            items(playgrounds, key = { it.id }) { playground ->
                FeldCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedId = playground.id },
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FeldHandDiamond(
                                color = MaterialTheme.disco.accent.copy(alpha = 0.8f),
                                seed = playground.title.hashCode(),
                                size = 10.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            FeldDistressedTitle(
                                text = playground.title,
                                spec = FeldDistressedTitles.Large,
                            )
                        }
                        Text(
                            text = playground.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
