package com.kape.connection.domain.usecase

import com.kape.connection.vpnServer
import com.kape.contracts.ConnectionManager
import com.kape.contracts.SnoozeStatus
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ConnectUseCaseTest {
    private val optimal = vpnServer("optimal")
    private val connectionManager = mockk<ConnectionManager>(relaxed = true) { every { isConnectionInProgress() } returns false }
    private val snooze = mockk<SnoozeStatus>(relaxed = true)
    private val getOptimal = mockk<GetOptimalVpnServerUseCase> { coEvery { this@mockk() } returns optimal }

    @Test
    fun `connects to the given server after ending the snooze`() =
        runTest {
            val de = vpnServer("de")

            ConnectUseCase(connectionManager, snooze, getOptimal, backgroundScope)(de, onProtocolNotAvailable = {}).join()

            coVerifyOrder {
                snooze.cancelSnooze()
                connectionManager.connect(de, true, any(), any())
            }
        }

    @Test
    fun `without a server, or for the auto region, connects to the optimal one`() =
        runTest {
            val connect = ConnectUseCase(connectionManager, snooze, getOptimal, backgroundScope)

            connect(null, onProtocolNotAvailable = {}).join()
            connect(vpnServer("auto", hasEndpoints = false), onProtocolNotAvailable = {}).join()

            coVerify(exactly = 2) { connectionManager.connect(optimal, true, any(), any()) }
        }

    @Test
    fun `a connection still in progress is stopped first`() =
        runTest {
            every { connectionManager.isConnectionInProgress() } returns true
            val de = vpnServer("de")

            ConnectUseCase(connectionManager, snooze, getOptimal, backgroundScope)(de, onProtocolNotAvailable = {}).join()

            coVerifyOrder {
                connectionManager.disconnect()
                connectionManager.connect(de, true, any(), any())
            }
        }
}