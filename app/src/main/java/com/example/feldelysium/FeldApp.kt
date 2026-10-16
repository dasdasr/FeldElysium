package com.example.feldelysium

import android.app.Application
import com.example.feldelysium.di.AppContainer

class FeldApp : Application() {
    val container by lazy { AppContainer(this) }
}
