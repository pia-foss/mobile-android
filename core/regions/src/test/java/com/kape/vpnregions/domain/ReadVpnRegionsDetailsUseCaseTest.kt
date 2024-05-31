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
import kotlin.test.assertEquals

class ReadVpnRegionsDetailsUseCaseTest {
    private val regionInputStream: RegionInputStream = mockk()
    private val regionSerialization: RegionSerialization = mockk()

    private lateinit var useCase: ReadVpnRegionsDetailsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ReadVpnRegionsDetailsUseCase(regionInputStream, regionSerialization)
    }

    @Test
    fun `test readVpnRegionsDetailsFromAssetsFolder`() = runTest {
        val response = VpnRegionsResponse()
        val stringResponse = Json.encodeToString(response)

        every { regionInputStream.readAssetsFile(any()) } returns stringResponse
        every { regionSerialization.decodeVpnRegionsFromString(any()) } returns response

        val actual = useCase.readVpnRegionsDetailsFromAssetsFolder()
        assertEquals(0, actual.size)
    }
}