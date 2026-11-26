package com.kape.connection.domain.usecase

import com.kape.connection.model.QuickConnectServer
import com.kape.connection.vpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveQuickConnectServersUseCaseTest {
    @Test
    fun `combines recent servers, favorites and the server list`() =
        runTest {
            val connectionPrefs =
                mockk<ConnectionStore> {
                    every { quickConnectServers } returns
                        MutableStateFlow(listOf(QuickConnectServer("de")))
                }
            val vpnRegionPrefs =
                mockk<VpnRegionStore> {
                    every { favoriteVpnServers } returns
                        MutableStateFlow(listOf(ServerData("Fr", isDip = false)))
                }
            val observeServers =
                mockk<ObserveVpnServersUseCase> {
                    every { this@mockk() } returns
                        MutableStateFlow(listOf(vpnServer("de"), vpnServer("fr")))
                }

            val items = ObserveQuickConnectServersUseCase(connectionPrefs, vpnRegionPrefs, observeServers)().first()

            assertEquals(listOf("fr" to true, "de" to false), items.map { it.server.key to it.isFavorite })
        }
}