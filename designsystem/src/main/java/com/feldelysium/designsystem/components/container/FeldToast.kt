package com.feldelysium.designsystem.components.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.content.FeldDiamond
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldPalette
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val ToastRough = RoughStyle(roughness = 1.dp, corner = 3.dp, tornChance = 0.15f)

@Composable
internal fun FeldToast(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = FeldPalette.Orange,
    seed: Int = rememberSeed(),
) {
    FeldCard(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        seed = seed,
        rough = ToastRough,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeldDiamond(accent, size = 12.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Stable
public class FeldToastState {
    public class Message(
        public val id: Long,
        public val text: String,
        public val accent: Color?,
        public val durationMillis: Long,
    )

    public var current: Message? by mutableStateOf<Message?>(null)
        private set
    private var counter = 0L

    public fun show(text: String, accent: Color? = null, durationMillis: Long = 2500L) {
        current = Message(++counter, text, accent, durationMillis)
    }

    internal fun dismiss(id: Long) {
        if (current?.id == id) current = null
    }
}

@Composable
public fun rememberFeldToastState(): FeldToastState = remember { FeldToastState() }

@Composable
public fun FeldToastHost(state: FeldToastState, modifier: Modifier = Modifier) {
    val message = state.current
    var last by remember { mutableStateOf<FeldToastState.Message?>(null) }

    LaunchedEffect(message?.id) {
        message ?: return@LaunchedEffect
        last = message
        delay(message.durationMillis.milliseconds)
        state.dismiss(message.id)
    }

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        enter = slideInVertically { -it / 2 } + fadeIn(),
        exit = slideOutVertically { -it / 2 } + fadeOut(),
    ) {
        (message ?: last)?.let {
            FeldToast(
                text = it.text,
                accent = it.accent ?: FeldPalette.Orange,
                seed = rememberSeed(it.id),
            )
        }
    }
}
