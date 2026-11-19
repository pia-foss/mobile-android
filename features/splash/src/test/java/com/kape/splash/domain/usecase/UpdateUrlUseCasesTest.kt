package com.kape.splash.domain.usecase

import com.kape.featureflags.domain.FeatureFlagRepository
import com.kape.featureflags.domain.usecase.IsForceUpdateRequiredUseCase
import com.kape.splash.domain.AppRelease
import com.kape.splash.domain.AppUpdateRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class UpdateUrlUseCasesTest {
    private var latestReleaseUrl: String? = "https://example.com/latest.apk"

    private val updates =
        object : AppUpdateRepository {
            override suspend fun getLatestRelease() = latestReleaseUrl?.let { AppRelease(it, versionCode = 1) }
        }

    private fun requiredUpdateUrl(
        forceUpdate: Boolean,
        storeUrl: String = "",
    ) = GetRequiredUpdateUrlUseCase(IsForceUpdateRequiredUseCase(Flags(forceUpdate)), updates, storeUrl)

    @Test
    fun `no update is required without the force-update flag`() =
        runTest {
            assertNull(requiredUpdateUrl(forceUpdate = false)())
        }

    @Test
    fun `no update is required when there is no release to update to`() =
        runTest {
            latestReleaseUrl = null

            assertNull(requiredUpdateUrl(forceUpdate = true)())
        }

    @Test
    fun `a required update uses the store page when the build has one`() =
        runTest {
            assertEquals("market://store", requiredUpdateUrl(forceUpdate = true, storeUrl = "market://store")())
        }

    @Test
    fun `a required update falls back to the latest release`() =
        runTest {
            assertEquals("https://example.com/latest.apk", requiredUpdateUrl(forceUpdate = true)())
        }

    @Test
    fun `the update button prefers the store page, then the latest release`() =
        runTest {
            assertEquals("market://store", GetUpdateUrlUseCase(updates, "market://store")())
            assertEquals("https://example.com/latest.apk", GetUpdateUrlUseCase(updates, "")())
        }

    @Test
    fun `the update button has no url without a store page or a release`() =
        runTest {
            latestReleaseUrl = null

            assertNull(GetUpdateUrlUseCase(updates, "")())
        }
}

private class Flags(
    forceUpdate: Boolean,
) : FeatureFlagRepository {
    private val flags = if (forceUpdate) listOf("force-update-required") else emptyList()

    override fun observeFlags() = flowOf(flags)

    override suspend fun refresh() = Unit
}