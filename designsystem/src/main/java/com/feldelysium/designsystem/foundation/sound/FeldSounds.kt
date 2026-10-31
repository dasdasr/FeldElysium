package com.feldelysium.designsystem.foundation.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

public interface FeldSounds {
    public fun click(seed: Int = 0)
    public fun select()
    public fun toggle(seed: Int = 0)
    public fun cancel()
    public fun save()
    public fun delete()
    public fun tab(seed: Int = 0)
}

internal object NoFeldSounds : FeldSounds {
    override fun click(seed: Int) = Unit
    override fun select() = Unit
    override fun toggle(seed: Int) = Unit
    override fun cancel() = Unit
    override fun save() = Unit
    override fun delete() = Unit
    override fun tab(seed: Int) = Unit
}

public val LocalFeldSounds: ProvidableCompositionLocal<FeldSounds> = staticCompositionLocalOf<FeldSounds> {
    NoFeldSounds
}

public val feldSounds: FeldSounds
    @Composable
    @ReadOnlyComposable
    get() = LocalFeldSounds.current
