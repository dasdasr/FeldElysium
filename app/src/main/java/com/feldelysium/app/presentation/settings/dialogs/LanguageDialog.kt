package com.feldelysium.app.presentation.settings.dialogs

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.feldelysium.app.R
import com.feldelysium.app.presentation.shared.language.AppLanguage
import com.feldelysium.app.presentation.shared.language.displayName
import com.feldelysium.designsystem.components.button.FeldButton
import com.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.feldelysium.designsystem.components.container.FeldDialog
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.sound.rememberFeldSounds
import com.feldelysium.designsystem.theme.Alegreya
import com.feldelysium.designsystem.theme.disco
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sign

private val PageWidth = 190.dp
private val TickGap = 80.dp

@Suppress("PropertyName")
private const val MinorPerMajor = 5
private val NeighborPull = 32.dp

private val Languages = AppLanguage.entries

@Composable
fun LanguageDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    val haptics = rememberFeldHaptics()
    val scope = rememberCoroutineScope()
    val accent = MaterialTheme.disco.accent
    val sounds = rememberFeldSounds()
    val applySeed = rememberSeed("apply")
    val pager = rememberPagerState(initialPage = Languages.indexOf(current)) { Languages.size }

    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.drop(1).collect { haptics.tick() }
    }

    fun goTo(page: Int) {
        haptics.tick()
        scope.launch { pager.animateScrollToPage(page) }
    }

    FeldDialog(
        title = stringResource(R.string.settings_language_title),
        eyebrow = stringResource(R.string.settings_language_eyebrow),
        onDismiss = onDismiss,
        footer = {
            FeldButton(
                text = stringResource(R.string.common_apply),
                onClick = {
                    sounds.click(applySeed)
                    val language = Languages[pager.currentPage]
                    if (language != current) onSelect(language)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                seed = applySeed,
                spec = FeldButtonDefaults.OrangeFresh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    ) {
        LanguageCarousel(pager, current, accent, onPageClick = ::goTo)

        Spacer(Modifier.height(36.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TapeKey(forward = false, enabled = pager.currentPage > 0) { goTo(pager.currentPage - 1) }
            TunerScale(
                pager = pager,
                accent = accent,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            )
            TapeKey(forward = true, enabled = pager.currentPage < Languages.lastIndex) {
                goTo(pager.currentPage + 1)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LanguageCarousel(pager: PagerState, current: AppLanguage, accent: Color, onPageClick: (Int) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(170.dp),
    ) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = (maxWidth - PageWidth) / 2),
            pageSize = PageSize.Fixed(PageWidth),
        ) { page ->
            LanguagePage(
                language = Languages[page],
                isCurrent = Languages[page] == current,
                isCentered = pager.currentPage == page,
                accent = accent,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val position = (page - pager.currentPage) - pager.currentPageOffsetFraction
                        val d = position.absoluteValue.coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.62f, d)
                        scaleY = scaleX
                        alpha = lerp(1f, 0.4f, d)
                        translationX = -position.sign * NeighborPull.toPx() * d
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onPageClick(page) },
            )
        }
    }
}

@Composable
private fun LanguagePage(
    language: AppLanguage,
    isCurrent: Boolean,
    isCentered: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val system = remember { systemLocale(context) }
    val native = language.displayName()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.common_current),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = accent,
            modifier = Modifier.graphicsLayer { alpha = if (isCurrent) 1f else 0f },
        )
        Text(
            text = native,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = Alegreya,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            text = captionName(language, native, system).lowercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
            ),
            color = if (isCentered) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun TunerScale(pager: PagerState, accent: Color, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val fling = PagerDefaults.flingBehavior(pager)
    val cream = MaterialTheme.colorScheme.secondary
    val ratio = with(LocalDensity.current) { PageWidth.toPx() / TickGap.toPx() }

    Canvas(
        modifier.draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { dx -> pager.dispatchRawDelta(-dx * ratio) },
            onDragStopped = { velocity ->
                scope.launch {
                    pager.scroll { with(fling) { performFling(-velocity * ratio) } }
                }
            },
        ),
    ) {
        val gap = TickGap.toPx()
        val step = gap / MinorPerMajor
        val cx = size.width / 2f
        val cy = size.height / 2f
        val shift = (pager.currentPage + pager.currentPageOffsetFraction) * gap
        val lastTick = MinorPerMajor * (Languages.size - 1)

        val first = floor((shift - cx) / step).toInt().coerceAtLeast(-3)
        val last = ceil((shift + cx) / step).toInt().coerceAtMost(lastTick + 3)

        for (i in first..last) {
            val x = cx + i * step - shift
            val major = i % MinorPerMajor == 0
            val fade = 1f - (abs(x - cx) / cx).coerceIn(0f, 1f) * 0.85f
            val h = if (major) 22.dp.toPx() else 10.dp.toPx()
            drawLine(
                color = cream.copy(alpha = fade * if (major) 0.55f else 0.3f),
                start = Offset(x, cy - h / 2f),
                end = Offset(x, cy + h / 2f),
                strokeWidth = 1.dp.toPx(),
            )
        }

        drawLine(
            color = accent,
            start = Offset(cx, cy - 17.dp.toPx()),
            end = Offset(cx, cy + 17.dp.toPx()),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun TapeKey(forward: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val shape = RoundedCornerShape(6.dp)
    val description = stringResource(if (forward) R.string.language_next else R.string.language_previous)

    Box(
        Modifier
            .size(44.dp)
            .graphicsLayer {
                translationY = if (pressed) 2.dp.toPx() else 0f
                alpha = if (enabled) 1f else 0.3f
            }
            .background(Color.White.copy(alpha = if (pressed) 0.02f else 0.07f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fe_play_next),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { scaleX = if (forward) 1f else -1f },
        )
    }
}

private fun captionName(language: AppLanguage, native: String, system: Locale): String {
    val target = language.tag?.let { Locale.forLanguageTag(it) } ?: system
    var name = target.getDisplayLanguage(system)
    if (name.equals(native, ignoreCase = true)) name = target.getDisplayLanguage(Locale.ENGLISH)
    return name.replaceFirstChar { it.uppercase() }
}

private fun systemLocale(context: Context): Locale = if (Build.VERSION.SDK_INT >= 33) {
    context.getSystemService(LocaleManager::class.java).systemLocales[0]
} else {
    Resources.getSystem().configuration.locales[0]
}
