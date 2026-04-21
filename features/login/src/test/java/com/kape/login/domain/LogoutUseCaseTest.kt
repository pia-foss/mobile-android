package com.kape.login.domain

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionManager
import com.kape.data.auth.ApiError
import com.kape.data.auth.ApiResult
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.KpiPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.login.BaseTest
import com.kape.login.domain.mobile.LogoutUseCaseImpl
import com.kape.payments.SubscriptionPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class LogoutUseCaseTest : BaseTest() {
    private val source = mockk<AuthenticationDataSource>()
    private val connectionManager = mockk<ConnectionManager>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>()
    private val csiPrefs = mockk<CsiPrefs>()
    private val customizationPrefs = mockk<CustomizationPrefs>()
    private val dipPrefs = mockk<DipPrefs>()
    private val networkManagementPrefs = mockk<NetworkManagementPrefs>()
    private val subscriptionPrefs = mockk<SubscriptionPrefs>()
    private val shadowsocksRegionPrefs = mockk<ShadowsocksRegionPrefs>()
    private val vpnRegionPrefs = mockk<VpnRegionPrefs>()
    private val settingsPrefs = mockk<SettingsPrefs>()
    private val kpiPrefs = mockk<KpiPrefs>()
    private val consentPrefs = mockk<ConsentPrefs>()
    private val ratingPrefs = mockk<RatingPrefs>()

    private lateinit var useCase: LogoutUseCaseImpl

    @BeforeEach
    internal fun setUp() {
        useCase =
            LogoutUseCaseImpl(
                source,
                connectionPrefs,
                csiPrefs,
                customizationPrefs,
                dipPrefs,
                networkManagementPrefs,
                subscriptionPrefs,
                shadowsocksRegionPrefs,
                vpnRegionPrefs,
                settingsPrefs,
                kpiPrefs,
                consentPrefs,
                ratingPrefs,
                connectionManager,
            )
    }

    @ParameterizedTest(name = "automationEnabled: {0}, result: {1}, expected: {2}")
    @MethodSource("data")
    fun logout(
        isAutomationEnabled: Boolean,
        result: ApiResult,
        expected: Boolean,
    ) = runTest {
        coEvery { source.logout() } returns result
        coEvery { connectionManager.disconnect() } returns Result.success(Unit)
        every { settingsPrefs.isAutomationEnabled() } returns isAutomationEnabled
        every { connectionPrefs.disconnectedByUser(any()) } returns Unit
        every { connectionPrefs.clear() } returns Unit
        every { csiPrefs.clear() } returns Unit
        every { customizationPrefs.clear() } returns Unit
        every { dipPrefs.clear() } returns Unit
        every { networkManagementPrefs.clear() } returns Unit
        every { subscriptionPrefs.clear() } returns Unit
        every { shadowsocksRegionPrefs.clear() } returns Unit
        every { vpnRegionPrefs.clear() } returns Unit
        every { settingsPrefs.clear() } returns Unit
        every { kpiPrefs.clear() } returns Unit
        every { consentPrefs.clear() } returns Unit
        every { ratingPrefs.clear() } returns Unit

        val actual = useCase.logout()
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun data() =
            Stream.of(
                Arguments.of(true, ApiResult.Success, true),
                Arguments.of(false, ApiResult.Success, true),
                Arguments.of(true, ApiResult.Error(ApiError.AuthFailed), false),
                Arguments.of(false, ApiResult.Error(ApiError.AccountExpired), false),
                Arguments.of(true, ApiResult.Error(ApiError.Throttled), false),
                Arguments.of(false, ApiResult.Error(ApiError.Unknown), false),
            )
    }
}