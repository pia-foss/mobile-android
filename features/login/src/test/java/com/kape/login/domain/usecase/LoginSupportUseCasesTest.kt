package com.kape.login.domain.usecase

import com.kape.account.domain.LoginResult
import com.kape.contracts.AppInfo
import com.kape.contracts.DeviceInfo
import com.kape.featureflags.domain.FeatureFlagRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URLDecoder

internal class LoginSupportUseCasesTest {
    @Test
    fun `the support dialog follows its feature flag`() =
        runTest {
            assertTrue(IsLoginSupportEnabledUseCase(Flags("other", "login-support-dialog"))())
            assertFalse(IsLoginSupportEnabledUseCase(Flags("other"))())
        }

    @Test
    fun `the support ticket describes the failure, app and device with a masked account`() {
        val url =
            BuildLoginSupportTicketUrlUseCase(App, Device)(
                LoginResult.ServiceUnavailable(503, "Service down"),
                username = "p1234567",
            )

        val description = URLDecoder.decode(url.substringAfter("tf_description="), "UTF-8")
        listOf(
            "Error code: 503",
            "Error message: Service down",
            "App version: 4.0.0 (400)",
            "Android OS version: 15",
            "Device: Google Pixel 9",
            "Account: p12****67",
        ).forEach { line -> assertTrue(line in description, "missing \"$line\" in:\n$description") }
        assertFalse("p1234567" in description)
    }
}

private class Flags(
    vararg flags: String,
) : FeatureFlagRepository {
    private val flags = flags.toList()

    override fun observeFlags() = flowOf(flags)

    override suspend fun refresh() = Unit
}

private object App : AppInfo {
    override val buildFlavor = "google"
    override val buildType = "debug"
    override val versionName = "4.0.0"
    override val versionCode = 400
    override val isFreshInstall = false
}

private object Device : DeviceInfo {
    override val manufacturer = "Google"
    override val model = "Pixel 9"
    override val osVersion = "15"
}