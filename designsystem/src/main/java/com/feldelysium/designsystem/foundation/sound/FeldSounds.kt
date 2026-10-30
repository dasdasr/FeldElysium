package com.feldelysium.designsystem.foundation.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface FeldSounds {
    fun click(seed: Int = 0)
    fun select()
    fun toggle(seed: Int = 0)
    fun cancel()
    fun save()
    fun delete()
    fun tab(seed: Int = 0)
}

object NoFeldSounds : FeldSounds {
    override fun click(seed: Int) = Unit
    override fun select() = Unit
    override fun toggle(seed: Int) = Unit
    override fun cancel() = Unit
    override fun save() = Unit
    override fun delete() = Unit
    override fun tab(seed: Int) = Unit
}

val LocalFeldSounds = staticCompositionLocalOf<FeldSounds> { NoFeldSounds }

@Composable
fun rememberFeldSounds(): FeldSounds = LocalFeldSounds.current
