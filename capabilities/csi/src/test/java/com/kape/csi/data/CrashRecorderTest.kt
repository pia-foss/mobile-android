package com.kape.csi.data

import com.kape.localprefs.prefs.CsiPrefs
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CrashRecorderTest {
    private val prefs = mockk<CsiPrefs>(relaxed = true)
    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    @BeforeEach
    fun setUp() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
    }

    @AfterEach
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
    }

    @Test
    fun `stores the crash, then hands it to the previous handler`() {
        var handled: Throwable? = null
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> handled = throwable }
        val crash = IllegalStateException("boom")

        CrashRecorder(prefs).install()
        Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), crash)

        coVerify { prefs.setLastKnownException(crash.stackTraceToString()) }
        assertSame(crash, handled)
    }

    @Test
    fun `stores the crash when there was no previous handler`() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        val crash = IllegalStateException("boom")

        CrashRecorder(prefs).install()
        Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), crash)

        coVerify { prefs.setLastKnownException(crash.stackTraceToString()) }
    }
}