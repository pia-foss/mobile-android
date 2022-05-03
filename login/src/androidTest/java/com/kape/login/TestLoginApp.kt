package com.kape.login

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class TestLoginApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}