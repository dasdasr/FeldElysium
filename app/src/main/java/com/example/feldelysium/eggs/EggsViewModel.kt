package com.example.feldelysium.eggs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

class EggsViewModel(application: Application) : AndroidViewModel(application) {
    val eggs = EasterEggs(
        eggs = EggRegistry.all,
        store = EggStore(application),
        scope = viewModelScope,
    )
}
