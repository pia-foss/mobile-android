package com.kape.vpnregionselection.domain.usecase

import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import com.kape.vpnregionselection.vpnServer
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ToggleFavoriteVpnRegionUseCaseTest {
    private val prefs = mockk<VpnRegionStore>(relaxed = true)

    @Test
    fun `a region that is not a favorite is added`() =
        runTest {
            every { prefs.isFavorite(ServerData("De", isDip = false)) } returns flowOf(false)

            ToggleFavoriteVpnRegionUseCase(prefs)(vpnServer("de"))

            coVerify { prefs.addToFavorites(ServerData("De", isDip = false)) }
            coVerify(exactly = 0) { prefs.removeFromFavorites(any()) }
        }

    @Test
    fun `a favorite region is removed`() =
        runTest {
            every { prefs.isFavorite(ServerData("De", isDip = false)) } returns flowOf(true)

            ToggleFavoriteVpnRegionUseCase(prefs)(vpnServer("de"))

            coVerify { prefs.removeFromFavorites(ServerData("De", isDip = false)) }
            coVerify(exactly = 0) { prefs.addToFavorites(any()) }
        }

    @Test
    fun `a dedicated IP is toggled separately from its region`() =
        runTest {
            every { prefs.isFavorite(ServerData("De", isDip = true)) } returns flowOf(false)

            ToggleFavoriteVpnRegionUseCase(prefs)(vpnServer("de", dedicatedIp = "1.2.3.4"))

            coVerify { prefs.addToFavorites(ServerData("De", isDip = true)) }
        }
}