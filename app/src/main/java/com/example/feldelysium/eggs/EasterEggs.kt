package com.example.feldelysium.eggs

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class EasterEggs(
    private val eggs: List<Egg>,
    private val store: EggStore,
    private val scope: CoroutineScope,
    private val config: EggConfig = EggConfig(),
    private val random: Random = Random.Default,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    class ActiveToast(val id: Long, val eggId: String, val reply: EggReply, val durationMillis: Long)

    private val _toast = MutableStateFlow<ActiveToast?>(null)
    val toast: StateFlow<ActiveToast?> = _toast.asStateFlow()

    private val _suppressed = MutableStateFlow(false)
    val suppressed: StateFlow<Boolean> = _suppressed.asStateFlow()

    private val history = ArrayDeque<AppEvent>()
    private val sessionCounts = HashMap<String, Int>()
    private val shownInSession = HashMap<String, Int>()
    private val lastShownAt = HashMap<String, Long>()
    private val lastRollAt = HashMap<String, Long>()
    private val lastReplyIndex = HashMap<String, Int>()
    private val recentToasts = ArrayDeque<Long>()
    private var lastAnyShownAt = 0L
    private var nextToastId = 0L

    private var discovered: Set<String> = emptySet()
    private var daily: MutableMap<String, MutableSet<Int>> = HashMap()
    private var loadedDay = 0L
    private val ready = CompletableDeferred<Unit>()

    init {
        scope.launch {
            val snapshot = store.load(epochDayOf(now()))
            discovered = snapshot.discovered
            daily = snapshot.daily.mapValuesTo(HashMap()) { it.value.toMutableSet() }
            loadedDay = snapshot.epochDay
            ready.complete(Unit)
        }
    }

    suspend fun awaitReady() = ready.await()

    fun setSuppressed(value: Boolean) {
        _suppressed.value = value
        if (value) _toast.value = null
    }

    fun dismiss(id: Long) {
        if (_toast.value?.id == id) _toast.value = null
    }

    fun report(key: String, value: String? = null): Boolean {
        val t = now()
        val event = AppEvent(key, value, t)

        history.addLast(event)
        while (history.size > 64 ||
            (history.isNotEmpty() && t - history.first().atMillis > 10.minutes.inWholeMilliseconds)
        ) history.removeFirst()
        sessionCounts[key] = (sessionCounts[key] ?: 0) + 1

        if (_suppressed.value || !ready.isCompleted) return false

        val ctx = contextFor(event, t)
        if (ctx.epochDay != loadedDay) {
            daily.clear()
            loadedDay = ctx.epochDay
        }

        val candidates = eggs
            .filter { it.trigger(ctx) && isEligible(it, t) }
            .sortedByDescending { rank(it) }

        for (egg in candidates) {
            if (egg.rarity != EggRarity.Secret && isGloballyBlocked(t)) continue
            val (index, reply) = pickReply(egg, ctx) ?: continue

            if (egg.chance < 1f) {
                val lastRoll = lastRollAt[egg.id]
                if (lastRoll != null && t - lastRoll < config.rollGap.inWholeMilliseconds) continue
                lastRollAt[egg.id] = t
                if (random.nextFloat() > egg.chance) continue
            }

            show(egg, index, reply, ctx, t)
            return true
        }
        return false
    }

    private fun isEligible(egg: Egg, t: Long): Boolean {
        if (egg.rarity == EggRarity.Secret && egg.id in discovered) return false
        lastShownAt[egg.id]?.let { if (t - it < egg.cooldown.inWholeMilliseconds) return false }
        egg.maxPerSession?.let { if ((shownInSession[egg.id] ?: 0) >= it) return false }
        dailyLimit(egg)?.let { if ((daily[egg.id]?.size ?: 0) >= it) return false }
        return true
    }

    private fun dailyLimit(egg: Egg): Int? = egg.perDay ?: when (egg.rarity) {
        EggRarity.Rare -> egg.replies.size.coerceAtLeast(1)
        EggRarity.UltraRare -> 1
        else -> null
    }

    private fun isGloballyBlocked(t: Long): Boolean {
        if (t - lastAnyShownAt < config.globalMinGap.inWholeMilliseconds) return true
        val windowMs = config.window.inWholeMilliseconds
        while (recentToasts.isNotEmpty() && t - recentToasts.first() > windowMs) recentToasts.removeFirst()
        return recentToasts.size >= config.maxPerWindow
    }

    private fun rank(egg: Egg): Int = egg.priority * 10 + when (egg.rarity) {
        EggRarity.Secret -> 3
        EggRarity.UltraRare -> 2
        EggRarity.Rare -> 1
        EggRarity.Frequent -> 0
    }

    private fun pickReply(egg: Egg, ctx: EggContext): Pair<Int, EggReply>? {
        val limitedPerDay = egg.rarity == EggRarity.Rare || egg.rarity == EggRarity.UltraRare
        val shownToday = if (limitedPerDay) daily[egg.id].orEmpty() else emptySet()

        val available = egg.replies.withIndex().filter { it.index !in shownToday }
        if (available.isEmpty()) return null

        val value = ctx.event.value
        val scoped = available.filter { it.value.scope != null && it.value.scope.equals(value, true) }
        val generic = available.filter { it.value.scope == null }

        val pool = when {
            scoped.isNotEmpty() && (generic.isEmpty() || random.nextFloat() < 0.65f) -> scoped
            generic.isNotEmpty() -> generic
            else -> return null
        }

        val last = lastReplyIndex[egg.id]
        val fresh = pool.filter { it.index != last }
        val choice = fresh.ifEmpty { pool }.random(random)
        return choice.index to choice.value
    }

    private fun show(egg: Egg, index: Int, reply: EggReply, ctx: EggContext, t: Long) {
        lastShownAt[egg.id] = t
        lastAnyShownAt = t
        recentToasts.addLast(t)
        shownInSession[egg.id] = (shownInSession[egg.id] ?: 0) + 1
        lastReplyIndex[egg.id] = index
        history.removeAll { it.key == ctx.event.key }

        when (egg.rarity) {
            EggRarity.Secret -> {
                discovered = discovered + egg.id
                scope.launch { store.markDiscovered(egg.id) }
            }

            EggRarity.Rare, EggRarity.UltraRare -> {
                daily.getOrPut(egg.id) { mutableSetOf() } += index
                val day = ctx.epochDay
                scope.launch { store.markShownToday(egg.id, index, day) }
            }

            EggRarity.Frequent -> Unit
        }

        _toast.value = ActiveToast(++nextToastId, egg.id, reply, config.toastDurationMillis)
    }

    private fun contextFor(event: AppEvent, t: Long): EggContext {
        val calendar = Calendar.getInstance().apply { timeInMillis = t }
        return EggContext(
            event = event,
            history = history.toList(),
            sessionCounts = sessionCounts,
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            epochDay = epochDayOf(t),
        )
    }

    private fun epochDayOf(millis: Long): Long =
        (millis + TimeZone.getDefault().getOffset(millis)) / 86_400_000L
}
