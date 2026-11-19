package com.kape.regions.vpn.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RefreshVpnServersUseCaseTest {
    private val repository = RecordingRepository()
    private val useCase = RefreshVpnServersUseCase(repository)

    @Test
    fun `fetches servers and pings when no latencies are known`() =
        runTest {
            useCase(locale = "en", isConnected = false)

            assertEquals(listOf("refreshServers(en)", "refreshLatencies"), repository.calls)
        }

    @Test
    fun `does nothing when latencies are known and the refresh isn't forced`() =
        runTest {
            repository.latenciesKnown = true

            useCase(locale = "en", isConnected = false)

            assertEquals(emptyList<String>(), repository.calls)
        }

    @Test
    fun `refreshes anyway when forced`() =
        runTest {
            repository.latenciesKnown = true

            useCase(locale = "en", isConnected = false, force = true)

            assertEquals(listOf("refreshServers(en)", "refreshLatencies"), repository.calls)
        }

    @Test
    fun `clears latencies instead of pinging while connected`() =
        runTest {
            repository.latenciesKnown = true

            useCase(locale = "en", isConnected = true)

            assertEquals(listOf("refreshServers(en)", "clearLatencies"), repository.calls)
        }
}

private class RecordingRepository : VpnRegionRepository {
    val calls = mutableListOf<String>()
    var latenciesKnown = false

    override fun observeServers(): Flow<List<VpnServer>> = emptyFlow()

    override fun observeIsDefaultList(): Flow<Boolean> = emptyFlow()

    override suspend fun refreshServers(locale: String) {
        calls += "refreshServers($locale)"
    }

    override suspend fun refreshLatencies() {
        calls += "refreshLatencies"
    }

    override fun clearLatencies() {
        calls += "clearLatencies"
    }

    override fun hasLatencies(): Boolean = latenciesKnown

    override suspend fun getServerInfo(): VpnServerInfo = VpnServerInfo()
}