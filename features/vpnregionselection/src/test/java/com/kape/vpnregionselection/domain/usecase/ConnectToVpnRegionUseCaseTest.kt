package com.kape.vpnregionselection.domain.usecase

import com.kape.contracts.ConnectionManager
import com.kape.contracts.SnoozeStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VpnRegionRepository
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import com.kape.vpnregionselection.vpnServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConnectToVpnRegionUseCaseTest {
    private val events = mutableListOf<String>()
    private val optimal = vpnServer("optimal")
    private val connection = RecordingConnectionManager(events)

    private fun TestScope.connectToRegion() =
        ConnectToVpnRegionUseCase(
            connection,
            RecordingSnooze(events),
            GetOptimalVpnServerUseCase(OptimalRepository(optimal)),
            backgroundScope,
        )

    @Test
    fun `connects to the picked region and ends the snooze`() =
        runTest {
            connectToRegion()(vpnServer("de")).join()

            assertEquals(listOf("cancel snooze", "connect de manual"), events)
        }

    @Test
    fun `the auto region connects to the optimal server`() =
        runTest {
            connectToRegion()(AUTO_REGION).join()

            assertEquals(listOf("cancel snooze", "connect optimal manual"), events)
        }

    @Test
    fun `a connection still in progress is stopped first`() =
        runTest {
            connection.inProgress = true

            connectToRegion()(vpnServer("de")).join()

            assertEquals(listOf("disconnect", "cancel snooze", "connect de manual"), events)
        }
}

private class RecordingConnectionManager(
    private val events: MutableList<String>,
) : ConnectionManager {
    var inProgress = false

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    ) {
        events += "connect ${server.key}" + if (isManual) " manual" else ""
    }

    override suspend fun connectToLastKnownOrOptimalServer() = Unit

    override suspend fun disconnect() {
        events += "disconnect"
        inProgress = false
    }

    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) = Unit

    override fun isConnectionInProgress() = inProgress
}

private class RecordingSnooze(
    private val events: MutableList<String>,
) : SnoozeStatus {
    override val snoozeEndTime: StateFlow<Long> = MutableStateFlow(0L)

    override fun cancelSnooze() {
        events += "cancel snooze"
    }
}

private class OptimalRepository(
    private val optimal: VpnServer,
) : VpnRegionRepository {
    override fun observeServers() = flowOf(listOf(optimal))

    override fun observeIsDefaultList() = flowOf(false)

    override suspend fun refreshServers(locale: String) = Unit

    override suspend fun refreshLatencies() = Unit

    override fun clearLatencies() = Unit

    override fun hasLatencies() = true

    override suspend fun getServerInfo(): VpnServerInfo = error("Not needed")
}