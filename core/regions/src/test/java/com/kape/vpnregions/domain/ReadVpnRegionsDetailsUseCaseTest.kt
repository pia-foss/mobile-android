package com.kape.vpnregions.domain

import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadVpnRegionsDetailsUseCaseTest {
    private val regionInputStream: RegionInputStream = mockk()
    private val regionSerialization: RegionSerialization = mockk()

    private lateinit var useCase: ReadVpnRegionsDetailsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ReadVpnRegionsDetailsUseCase(regionInputStream, regionSerialization)
    }

    @Test
    fun `test readVpnRegionsDetailsFromAssetsFolder`() =
        runTest {
            val response = VpnRegionsResponse()
            val stringResponse = Json.encodeToString(response)

            every { regionInputStream.readAssetsFile(any()) } returns stringResponse
            every { regionSerialization.decodeVpnRegionsFromString(any()) } returns response

            val actual = useCase.readVpnRegionsDetailsFromAssetsFolder()
            assertEquals(0, actual.size)
        }

    @Test
    fun `bundled vpn regions asset provides servers and openvpn ports`() {
        val asset = File("../../app/src/main/assets/vpn-regions.json").readText()
        every { regionInputStream.readAssetsFile(any()) } returns asset
        every { regionSerialization.decodeVpnRegionsFromString(any()) } answers
            { RegionSerialization().decodeVpnRegionsFromString(firstArg()) }

        val (servers, serverInfo) = useCase.readVpnRegionsDetailsAndServerInfoFromAssetsFolder()

        assertTrue(servers.isNotEmpty())
        assertTrue(serverInfo.isValid())
    }
}