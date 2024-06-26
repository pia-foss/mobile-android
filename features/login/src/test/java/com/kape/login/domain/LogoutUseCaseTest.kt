package com.kape.login.domain

import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.csi.CsiPrefs
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.dip.DipPrefs
import com.kape.login.BaseTest
import com.kape.login.domain.mobile.AuthenticationDataSource
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.networkmanagement.NetworkManagementPrefs
import com.kape.payments.SubscriptionPrefs
import com.kape.rating.prefs.RatingPrefs
import com.kape.settings.SettingsPrefs
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shareevents.KpiPrefs
import com.kape.signup.ConsentPrefs
import com.kape.utils.ApiError
import com.kape.utils.ApiResult
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.VpnRegionPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class LogoutUseCaseTest : BaseTest() {
    private val source = mockk<AuthenticationDataSource>()
    private val connectionUseCase = mockk<ConnectionUseCase>()
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

    private lateinit var useCase: LogoutUseCase

    @BeforeEach
    internal fun setUp() {
        useCase = LogoutUseCase(
            source,
            connectionPrefs,
            connectionUseCase,
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
        )
    }

    @ParameterizedTest(name = "automationEnabled: {0}, connectionActive: {1}, result: {2}, expected: {3}")
    @MethodSource("data")
    fun logout(
        isAutomationEnabled: Boolean,
        isConnectionActive: Boolean,
        result: ApiResult,
        expected: Boolean,
    ) = runTest {
        coEvery { source.logout() } returns flow {
            emit(result)
        }
        coEvery { connectionUseCase.stopConnection() } returns flow {
            emit(true)
        }
        every { connectionUseCase.isConnected() } returns isConnectionActive
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
        every { settingsPrefs.isAutomationEnabled() } returns isAutomationEnabled
        every { connectionPrefs.disconnectedByUser(any()) } returns Unit

        useCase.logout().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(true, true, ApiResult.Success, true),
            Arguments.of(true, false, ApiResult.Success, true),
            Arguments.of(false, true, ApiResult.Success, true),
            Arguments.of(false, false, ApiResult.Success, true),
            Arguments.of(true, true, ApiResult.Error(ApiError.AuthFailed), false),
            Arguments.of(false, false, ApiResult.Error(ApiError.AccountExpired), false),
            Arguments.of(true, false, ApiResult.Error(ApiError.Throttled), false),
            Arguments.of(false, true, ApiResult.Error(ApiError.Unknown), false),
        )
    }
}