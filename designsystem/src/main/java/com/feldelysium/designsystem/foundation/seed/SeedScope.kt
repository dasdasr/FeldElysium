package com.feldelysium.designsystem.foundation.seed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.random.Random

@Immutable
internal data class SeedScope(val base: Int) {
    fun child(key: Any?): SeedScope = SeedScope(mix(base, key.hashCode()))
    fun seedFor(position: Int, key: Any? = null): Int = mix(mix(base, position), key.hashCode())
}

internal val LocalSeedScope = staticCompositionLocalOf { SeedScope(0) }

@Composable
public fun ProvideSeedScope(sessionSeed: Int = rememberSaveable { Random.nextInt() }, content: @Composable () -> Unit) {
    val scope = remember(sessionSeed) { SeedScope(sessionSeed) }
    CompositionLocalProvider(LocalSeedScope provides scope, content = content)
}

@Composable
internal fun SeedBranch(key: Any?, content: @Composable () -> Unit) {
    val parent = LocalSeedScope.current
    val child = remember(parent, key) { parent.child(key) }
    CompositionLocalProvider(LocalSeedScope provides child, content = content)
}

@Composable
public fun rememberSeed(key: Any? = null): Int {
    val scope = LocalSeedScope.current
    val position = currentCompositeKeyHashCode
    return remember(scope, position, key) { scope.seedFor(position.toInt(), key) }
}

private fun mix(a: Int, b: Int): Int {
    var h = a xor (b * -0x61c88647)
    h = h xor (h ushr 16)
    h *= 0x85ebca6bL.toInt()
    h = h xor (h ushr 13)
    h *= 0xc2b2ae35L.toInt()
    return h xor (h ushr 16)
}
