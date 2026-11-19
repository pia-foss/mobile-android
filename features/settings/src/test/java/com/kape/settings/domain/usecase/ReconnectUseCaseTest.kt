package com.kape.settings.domain.usecase

import com.kape.contracts.ConnectionManager
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ReconnectUseCaseTest {
    private val connectionPrefs = mockk<ConnectionStore>()
    private val connectionManager = mockk<ConnectionManager>(relaxed = true)

    @Test
    fun `reconnects to the selected server, disconnecting when the connection stops`() =
        runTest {
            val server = mockk<VpnServer>()
            val stopCallback = slot<() -> Unit>()
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server
            coEvery { connectionManager.reconnect(server, capture(stopCallback)) } returns Unit

            ReconnectUseCase(connectionPrefs, connectionManager, backgroundScope)().join()
            coVerify(exactly = 0) { connectionManager.disconnect() }

            stopCallback.captured()
            // backgroundScope work isn't run by advanceUntilIdle.
            testScheduler.runCurrent()

            coVerify { connectionManager.disconnect() }
        }

    @Test
    fun `without a selected server nothing happens`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null

            ReconnectUseCase(connectionPrefs, connectionManager, backgroundScope)().join()

            coVerify(exactly = 0) { connectionManager.reconnect(any(), any()) }
        }
}