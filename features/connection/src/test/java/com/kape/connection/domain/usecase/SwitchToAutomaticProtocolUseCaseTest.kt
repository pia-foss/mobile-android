package com.kape.connection.domain.usecase

import com.kape.connection.vpnServer
import com.kape.contracts.ConnectionManager
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SwitchToAutomaticProtocolUseCaseTest {
    private val problemDetector = mockk<ConnectionProblemDetector>(relaxed = true)
    private val settingsPrefs = mockk<SettingsStore>(relaxed = true)
    private val connectionManager = mockk<ConnectionManager>(relaxed = true)

    @Test
    fun `switching selects the automatic protocol and reconnects to the current server`() =
        runTest {
            val de = vpnServer("de")

            SwitchToAutomaticProtocolUseCase(problemDetector, settingsPrefs, connectionManager, backgroundScope)(de).join()

            coVerifyOrder {
                problemDetector.onSwitchToAutomaticAccepted()
                settingsPrefs.setSelectedProtocol(VpnProtocols.Automatic)
                connectionManager.reconnect(de, any())
            }
        }

    @Test
    fun `without a server the protocol changes but nothing reconnects`() =
        runTest {
            SwitchToAutomaticProtocolUseCase(problemDetector, settingsPrefs, connectionManager, backgroundScope)(null).join()

            coVerify { settingsPrefs.setSelectedProtocol(VpnProtocols.Automatic) }
            coVerify(exactly = 0) { connectionManager.reconnect(any(), any()) }
        }
}