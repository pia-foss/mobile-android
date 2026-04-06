package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConnectionUseCaseTest {

    private val connectionSource: ConnectionDataSource = mockk(relaxed = true)
    private val connectionInfoProvider: ConnectionInfoProvider = mockk(relaxed = true)
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val startShadowsocksUseCase: StartShadowsocksUseCase = mockk()
    private val stopShadowsocksUseCase: StopShadowsocksUseCase = mockk(relaxed = true)
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase = mockk(relaxed = true)
    private val connectionStatusProvider: ConnectionStatusProvider = mockk(
        relaxed = true,
        extraInterfaces = arrayOf(VPNManagerConnectionListener::class),
    )
    private val startPortForwardingUseCase: StartPortForwardingUseCase = mockk(relaxed = true)
    private val stopPortForwardingUseCase: StopPortForwardingUseCase = mockk(relaxed = true)

    private val server: VpnServer = mockk(relaxed = true)

    private lateinit var startConnectionUseCase: StartConnectionUseCase
    private lateinit var stopConnectionUseCase: StopConnectionUseCase
    private lateinit var reconnectUseCase: ReconnectUseCase

    @BeforeEach
    fun setUp() {
        startConnectionUseCase = StartConnectionUseCase(
            connectionSource,
            connectionInfoProvider,
            connectionPrefs,
            startShadowsocksUseCase,
            stopShadowsocksUseCase,
            connectionConfigurationUseCase,
            connectionStatusProvider,
            startPortForwardingUseCase,
        )
        stopConnectionUseCase = StopConnectionUseCase(
            connectionInfoProvider,
            connectionSource,
            stopShadowsocksUseCase,
            stopPortForwardingUseCase,
        )
        reconnectUseCase = ReconnectUseCase(
            startConnectionUseCase,
            stopConnectionUseCase,
            connectionInfoProvider,
        )
    }

    // region StartConnectionUseCase

    @Test
    fun `startConnection - not in connect state - shadowsocks ok - returns connection result`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns false
        coEvery { startShadowsocksUseCase() } returns true
        coEvery { connectionSource.startConnection(any(), any()) } returns true

        val result = startConnectionUseCase(server, true)

        assertEquals(true, result)
    }

    @Test
    fun `startConnection - not in connect state - connection fails - stops shadowsocks`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns false
        coEvery { startShadowsocksUseCase() } returns true
        coEvery { connectionSource.startConnection(any(), any()) } returns false

        val result = startConnectionUseCase(server, true)

        assertEquals(false, result)
        coVerify(exactly = 1) { stopShadowsocksUseCase() }
    }

    @Test
    fun `startConnection - not in connect state - shadowsocks fails - returns false`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns false
        coEvery { startShadowsocksUseCase() } returns false

        val result = startConnectionUseCase(server, true)

        assertEquals(false, result)
        coVerify(exactly = 0) { connectionSource.startConnection(any(), any()) }
    }

    @Test
    fun `startConnection - already in connect state - returns false`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns true

        val result = startConnectionUseCase(server, true)

        assertEquals(false, result)
        coVerify(exactly = 0) { startShadowsocksUseCase() }
    }

    // endregion

    // region StopConnectionUseCase

    @Test
    fun `stopConnection - in connect state - returns connection source result`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns true
        coEvery { connectionSource.stopConnection() } returns true

        val result = stopConnectionUseCase()

        assertEquals(true, result)
    }

    @Test
    fun `stopConnection - in connect state - resets info and stops shadowsocks and port forwarding`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns true
        coEvery { connectionSource.stopConnection() } returns false

        stopConnectionUseCase()

        verify(exactly = 1) { connectionInfoProvider.resetConnectionInfo() }
        coVerify(exactly = 1) { stopShadowsocksUseCase() }
        verify(exactly = 1) { stopPortForwardingUseCase() }
    }

    @Test
    fun `stopConnection - not in connect state - returns false`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns false

        val result = stopConnectionUseCase()

        assertEquals(false, result)
        coVerify(exactly = 0) { connectionSource.stopConnection() }
    }

    // endregion

    // region ReconnectUseCase

    @Test
    fun `reconnect - when in connect state - stops then starts`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returnsMany listOf(true, false, false)
        coEvery { connectionSource.stopConnection() } returns true
        coEvery { startShadowsocksUseCase() } returns true
        coEvery { connectionSource.startConnection(any(), any()) } returns true

        reconnectUseCase(server)

        coVerify(exactly = 1) { connectionSource.stopConnection() }
        coVerify(exactly = 1) { connectionSource.startConnection(any(), any()) }
    }

    @Test
    fun `reconnect - when not in connect state - starts directly`() = runTest {
        every { connectionInfoProvider.isInConnectState() } returns false
        coEvery { startShadowsocksUseCase() } returns true
        coEvery { connectionSource.startConnection(any(), any()) } returns true

        reconnectUseCase(server)

        coVerify(exactly = 0) { connectionSource.stopConnection() }
        coVerify(exactly = 1) { connectionSource.startConnection(any(), any()) }
    }

    // endregion
}