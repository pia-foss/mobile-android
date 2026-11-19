package com.kape.buildconfig.data

import com.kape.contracts.AppInfo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BuildConfigProviderTest {
    private fun provider(
        flavor: String,
        type: String = "release",
    ) = BuildConfigProvider(
        object : AppInfo {
            override val buildFlavor = flavor
            override val buildType = type
            override val versionName = "1.0"
            override val versionCode = 1
            override val isFreshInstall = false
        },
    )

    @Test
    fun `recognises each store`() {
        assertTrue(provider("google").isGoogleFlavor())
        assertTrue(provider("amazon").isAmazonFlavor())
        assertTrue(provider("noinapp").isWebFlavor())
        assertTrue(provider("meta").isMetaFlavor())
        assertTrue(provider("fdroid").isFDroid())
        assertFalse(provider("fdroid").isGoogleFlavor())
    }

    @Test
    fun `recognises the build type`() {
        assertTrue(provider("google", "debug").isDebugType())
        assertTrue(provider("google", "release").isReleaseType())
    }

    @Test
    fun `fails fast on an unknown store, so a new flavor can't be forgotten here`() {
        assertThrows<IllegalArgumentException> { provider("huawei") }
    }
}