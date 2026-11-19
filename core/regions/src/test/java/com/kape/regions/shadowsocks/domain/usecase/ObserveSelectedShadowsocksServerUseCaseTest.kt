package com.kape.regions.shadowsocks.domain.usecase

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import com.kape.regions.shadowsocks.server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveSelectedShadowsocksServerUseCaseTest {
    private val repository = FakeShadowsocksRegionRepository()
    private val useCase = ObserveSelectedShadowsocksServerUseCase(repository)

    @Test
    fun `returns the user's pick with the latest details from the server list`() =
        runTest {
            val updated = server(host = "1.1.1.1", region = "UK Manchester")
            repository.servers.value = listOf(server(host = "2.2.2.2"), updated)
            repository.selected.value = server(host = "1.1.1.1", region = "outdated name")

            assertEquals(updated, useCase().first())
        }

    @Test
    fun `picks and saves the first server when the user never picked one`() =
        runTest {
            val first = server(host = "1.1.1.1")
            repository.servers.value = listOf(first, server(host = "2.2.2.2"))

            assertEquals(first, useCase().first())
            assertEquals(first, repository.selected.value)
        }

    @Test
    fun `picks and saves the first server when the user's pick is no longer offered`() =
        runTest {
            val first = server(host = "1.1.1.1")
            repository.servers.value = listOf(first)
            repository.selected.value = server(host = "9.9.9.9")

            assertEquals(first, useCase().first())
            assertEquals(first, repository.selected.value)
        }
}

private class FakeShadowsocksRegionRepository : ShadowsocksRegionRepository {
    val servers = MutableStateFlow<List<ShadowsocksServer>>(emptyList())
    val selected = MutableStateFlow<ShadowsocksServer?>(null)

    override fun observeServers(): Flow<List<ShadowsocksServer>> = servers

    override suspend fun refreshServers(locale: String) = Unit

    override fun observeSelectedServer(): Flow<ShadowsocksServer?> = selected

    override suspend fun selectServer(server: ShadowsocksServer) {
        selected.value = server
    }
}