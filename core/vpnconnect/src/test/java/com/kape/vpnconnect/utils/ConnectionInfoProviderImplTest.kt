package com.kape.vpnconnect.utils

import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.data.kpi.KpiConnectionStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.vpnconnect.domain.ClientStateDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Regression coverage for a bug where the KPI-submission collector on [vpnManagerConnectionStatus]
 * was nested inside the outer [ConnectionStatus] collector. Since that inner `collectLatest` never
 * completes, the code below it (getPublicIp/getVpnIp) was unreachable on every status transition.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionInfoProviderImplTest {
    private val connectionStatusProvider = mockk<ConnectionStatusProvider>(relaxed = true)
    private val clientStateDataSource = mockk<ClientStateDataSource>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val submitKpiEventUseCase = mockk<SubmitKpiEventUseCase>(relaxed = true)
    private val portForwardingUseCase = mockk<PortForwardingUseCase>(relaxed = true)

    // Neutral starting state: neither DISCONNECTED nor CONNECTED, so init doesn't trigger either IP call.
    private val statusFlow = MutableStateFlow<ConnectionStatus>(ConnectionStatus.CONNECTING)
    private val vpnManagerStatusFlow = MutableStateFlow<KapeVPNConnectionStatus?>(null)

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        every { connectionStatusProvider.status } returns statusFlow
        every { connectionStatusProvider.vpnManagerConnectionStatus } returns vpnManagerStatusFlow
        every { connectionPrefs.clientIp } returns MutableStateFlow("")
        every { connectionPrefs.vpnIp } returns MutableStateFlow("")
        coEvery { clientStateDataSource.getPublicIp() } returns "1.2.3.4"
        coEvery { clientStateDataSource.getVpnIp() } returns "10.0.0.1"
    }

    private fun createProvider() =
        ConnectionInfoProviderImpl(
            connectionStatusProvider = connectionStatusProvider,
            clientStateDataSource = clientStateDataSource,
            connectionPrefs = connectionPrefs,
            submitKpiEventUseCase = submitKpiEventUseCase,
            portForwardingUseCase = portForwardingUseCase,
            ioDispatcher = dispatcher,
            mainDispatcher = dispatcher,
        )

    @Test
    fun `status transitions to DISCONNECTED - fetches public ip`() =
        runTest {
            createProvider()

            statusFlow.value = ConnectionStatus.DISCONNECTED

            coVerify { clientStateDataSource.getPublicIp() }
            coVerify(exactly = 0) { clientStateDataSource.getVpnIp() }
        }

    @Test
    fun `status transitions to CONNECTED - fetches vpn ip`() =
        runTest {
            createProvider()

            statusFlow.value = ConnectionStatus.CONNECTED

            coVerify { clientStateDataSource.getVpnIp() }
            coVerify(exactly = 0) { clientStateDataSource.getPublicIp() }
        }

    @Test
    fun `vpn manager status updates independently of connection status - submits kpi event without blocking ip fetch on transition`() =
        runTest {
            createProvider()

            vpnManagerStatusFlow.value = KapeVPNConnectionStatus.Connecting
            statusFlow.value = ConnectionStatus.CONNECTED

            coVerify { submitKpiEventUseCase.submitConnectionEvent(KpiConnectionStatus.Connecting, any()) }
            coVerify { clientStateDataSource.getVpnIp() }
        }
}