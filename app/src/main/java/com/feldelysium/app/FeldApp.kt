package com.feldelysium.app

import android.app.Application
import com.feldelysium.app.di.AppContainer

class FeldApp : Application() {
    val container by lazy { AppContainer(this) }
}
