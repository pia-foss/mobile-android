package com.kape.connection.domain.usecase

import com.kape.connection.vpnServer
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import com.kape.regions.vpn.domain.usecase.ObserveIsDefaultVpnServerListUseCase
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveDisplayedServerUseCaseTest {
    private val de = vpnServer("de")
    private val optimal = vpnServer("optimal")
    private val selected = MutableStateFlow<VpnServer?>(null)
    private val servers = MutableStateFlow<List<VpnServer>>(emptyList())
    private val isDefaultList = MutableStateFlow(true)
    private var optimalServer = optimal

    private val useCase =
        ObserveDisplayedServerUseCase(
            connectionPrefs = mockk<ConnectionStore> { every { observeSelectedVpnServer() } returns selected },
            observeVpnServers = mockk<ObserveVpnServersUseCase> { every { this@mockk() } returns servers },
            observeIsDefaultList = mockk<ObserveIsDefaultVpnServerListUseCase> { every { this@mockk() } returns isDefaultList },
            getOptimalVpnServer = mockk<GetOptimalVpnServerUseCase> { coEvery { this@mockk() } answers { optimalServer } },
        )

    private fun TestScope.collect(): List<DisplayedServer> {
        val emitted = mutableListOf<DisplayedServer>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase().toList(emitted) }
        return emitted
    }

    @Test
    fun `nothing is shown until the server list has loaded`() =
        runTest {
            selected.value = de
            val emitted = collect()

            assertEquals(emptyList<DisplayedServer>(), emitted)

            servers.value = listOf(de, optimal)

            assertEquals(listOf(DisplayedServer(de, isOptimal = false, showOptimalLocationInfo = false)), emitted)
        }

    @Test
    fun `without a selection the optimal server is shown, with the info only for the default list`() =
        runTest {
            servers.value = listOf(de, optimal)
            val emitted = collect()

            isDefaultList.value = false

            assertEquals(
                listOf(
                    DisplayedServer(optimal, isOptimal = true, showOptimalLocationInfo = true),
                    DisplayedServer(optimal, isOptimal = true, showOptimalLocationInfo = false),
                ),
                emitted,
            )
        }

    @Test
    fun `the optimal server is picked again when the server list changes`() =
        runTest {
            servers.value = listOf(optimal)
            val emitted = collect()

            optimalServer = de
            servers.value = listOf(de)

            assertEquals(listOf(optimal, de), emitted.map { it.server })
        }

    @Test
    fun `selecting a server replaces the optimal one, and clearing it brings it back`() =
        runTest {
            servers.value = listOf(de, optimal)
            val emitted = collect()

            selected.value = de
            selected.value = null

            assertEquals(listOf(true, false, true), emitted.map { it.isOptimal })
        }
}