package com.pistolcaffe.nintendoapitest

import android.app.Application
import timber.log.Timber

class NintendoApplication : Application() {
    val repository by lazy { NintendoRepository() }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        app = this
    }

    companion object {
        lateinit var app: NintendoApplication
    }
}