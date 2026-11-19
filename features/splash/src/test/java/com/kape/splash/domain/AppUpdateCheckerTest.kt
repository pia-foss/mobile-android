package com.kape.splash.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AppUpdateCheckerTest {
    @Test
    fun `a newer release is an update`() {
        assertTrue(isUpdateAvailable(installedVersionCode = 100, latestVersionCode = 101, isGooglePlayBuild = false))
    }

    @Test
    fun `the installed release is not an update`() {
        assertFalse(isUpdateAvailable(installedVersionCode = 100, latestVersionCode = 100, isGooglePlayBuild = false))
    }

    @Test
    fun `Google Play builds compare against the release minus the version offset`() {
        assertFalse(isUpdateAvailable(installedVersionCode = 10_100, latestVersionCode = 10_100, isGooglePlayBuild = true))
        assertTrue(isUpdateAvailable(installedVersionCode = 10_100, latestVersionCode = 20_101, isGooglePlayBuild = true))
    }
}