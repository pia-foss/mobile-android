package com.kape.csi.data

import com.kape.localprefs.prefs.CsiPrefs
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Singleton

@Singleton
class CrashRecorder(
    private val csiPrefs: CsiPrefs,
) {
    fun install() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Blocking: the process is about to die, so the write has to finish first.
            runBlocking { csiPrefs.setLastKnownException(throwable.stackTraceToString()) }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}