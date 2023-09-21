package com.kape.dedicatedip.domain

import app.cash.turbine.test
import com.kape.regionselection.domain.GetRegionsUseCase
import com.kape.utils.server.Server
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerForDipUseCaseTest {

    private val getRegionsUseCase: GetRegionsUseCase = mockk()
    private val dip: DedicatedIPInformationResponse.DedicatedIPInformation =
        DedicatedIPInformationResponse.DedicatedIPInformation(
            id = "name",
            cn = "cn",
            ip = "key",
            dipToken = "dipToken",
            status = DedicatedIPInformationResponse.Status.active,
        )
    private lateinit var useCase: ServerForDipUseCase

    @BeforeEach
    fun setUp() {
        useCase = ServerForDipUseCase(getRegionsUseCase)
    }

    @ParameterizedTest(name = "response: {0}, expected: {1}")
    @MethodSource("data")
    fun getServerForDip(serverResponse: List<Server>, expected: Server?) = runTest {
        coEvery { getRegionsUseCase.loadRegions(any()) } returns flow {
            emit(serverResponse)
        }

        useCase.getServerForDip("en", dip).test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    companion object {
        private fun endpoints(): Map<Server.ServerGroup, List<Server.ServerEndpointDetails>> {
            val endpoints = mutableMapOf<Server.ServerGroup, List<Server.ServerEndpointDetails>>()
            endpoints[Server.ServerGroup.OPENVPN_TCP] =
                listOf(Server.ServerEndpointDetails("key", "cn"))
            return endpoints
        }

        val server = Server(
            name = "name",
            iso = "",
            endpoints = endpoints(),
            key = "key",
            isGeo = false,
            isOffline = false,
            isAllowsPF = false,
            latency = null,
            dedicatedIp = "key",
            dipToken = "dipToken",
            latitude = null,
            longitude = null,
        )
        val server2 = Server(
            name = "name2",
            iso = "",
            endpoints = endpoints(),
            key = "key2",
            isGeo = false,
            isOffline = false,
            isAllowsPF = false,
            latency = null,
            dedicatedIp = "key2",
            dipToken = "dipToken",
            latitude = null,
            longitude = null,
        )

        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(emptyList<Server>(), null),
            Arguments.of(listOf(server), server),
            Arguments.of(listOf(server2), null),
        )
    }
}