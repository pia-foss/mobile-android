package com.kape.settings.data

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InstalledAppsTest {
    private val labels = mutableMapOf<ApplicationInfo, String>()
    private val packageManager =
        mockk<PackageManager> { every { getApplicationLabel(any()) } answers { labels.getValue(firstArg()) } }
    private val context = mockk<Context> { every { packageManager } returns this@InstalledAppsTest.packageManager }

    private fun app(label: String): PackageInfo {
        val info = mockk<ApplicationInfo>()
        labels[info] = label
        return mockk<PackageInfo>().apply { applicationInfo = info }
    }

    @Test
    fun `lists apps using the internet sorted by label ignoring case, skipping packages without app info`() =
        runTest {
            val packages = listOf(app("zebra"), app("Alpha"), mockk<PackageInfo>().apply { applicationInfo = null }, app("beta"))
            every { packageManager.getPackagesHoldingPermissions(arrayOf(Manifest.permission.INTERNET), 0) } returns packages
            val installedApps = InstalledApps(context, StandardTestDispatcher(testScheduler))

            val apps = installedApps.load()

            assertEquals(listOf("Alpha", "beta", "zebra"), apps.map { installedApps.label(it) })
        }
}