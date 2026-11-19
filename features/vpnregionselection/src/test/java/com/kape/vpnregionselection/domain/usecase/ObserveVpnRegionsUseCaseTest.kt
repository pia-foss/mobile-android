package com.kape.vpnregionselection.domain.usecase

import com.kape.data.AUTO_KEY
import com.kape.data.RegionItemType
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import com.kape.regions.vpn.domain.VpnRegionRepository
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.vpnregionselection.vpnServer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveVpnRegionsUseCaseTest {
    private val servers = MutableStateFlow<List<VpnServer>>(emptyList())
    private val favorites = MutableStateFlow<List<ServerData>>(emptyList())
    private val protocol = MutableStateFlow(VpnProtocols.Automatic)
    private val showGeoLocated = MutableStateFlow(true)

    private val vpnRegionPrefs = mockk<VpnRegionStore> { every { favoriteVpnServers } returns favorites }
    private val settingsPrefs =
        mockk<SettingsStore> {
            every { selectedProtocol } returns protocol
            every { openVpnSettings } returns MutableStateFlow(OpenVpnSettings())
            every { isShowGeoLocatedServersEnabled } returns showGeoLocated
        }
    private val observeRegions =
        ObserveVpnRegionsUseCase(ObserveVpnServersUseCase(ServersRepository(servers)), vpnRegionPrefs, settingsPrefs)

    private suspend fun listedKeys() =
        observeRegions().first().map { item ->
            when (val type = item.type) {
                is RegionItemType.Content -> type.server.key + if (type.isFavorite) "*" else ""
                RegionItemType.HeadingFavorites -> "[favorites]"
                RegionItemType.HeadingAll -> "[all]"
            }
        }

    @Test
    fun `the auto region comes first, then dedicated IPs, then regions by latency`() =
        runTest {
            servers.value =
                listOf(
                    vpnServer("fr", latency = "80"),
                    vpnServer("de", latency = "20"),
                    vpnServer("dip", latency = "300", dedicatedIp = "1.2.3.4"),
                )

            assertEquals(listOf(AUTO_KEY, "dip", "de", "fr"), listedKeys())
        }

    @Test
    fun `favorites are listed on top as well as among all regions`() =
        runTest {
            servers.value = listOf(vpnServer("de", latency = "20"), vpnServer("fr", latency = "80"))
            favorites.value = listOf(ServerData("Fr", isDip = false))

            assertEquals(listOf("[favorites]", "fr*", "[all]", AUTO_KEY, "de", "fr*"), listedKeys())
        }

    @Test
    fun `regions that don't support the protocol, and unwanted geo-located ones, are left out`() =
        runTest {
            servers.value =
                listOf(
                    vpnServer("de", groups = setOf(VpnServer.ServerGroup.WIREGUARD)),
                    vpnServer("fr", groups = setOf(VpnServer.ServerGroup.OPENVPN_UDP)),
                    vpnServer("bs", isGeo = true),
                )
            protocol.value = VpnProtocols.WireGuard
            showGeoLocated.value = false

            assertEquals(listOf(AUTO_KEY, "de"), listedKeys())
        }

    @Test
    fun `the auto region can't be a favorite`() =
        runTest {
            val auto = observeRegions().first().first().type as RegionItemType.Content

            assertEquals(false, auto.enableFavorite)
        }
}

private class ServersRepository(
    private val servers: Flow<List<VpnServer>>,
) : VpnRegionRepository {
    override fun observeServers() = servers

    override fun observeIsDefaultList() = MutableStateFlow(false)

    override suspend fun refreshServers(locale: String) = Unit

    override suspend fun refreshLatencies() = Unit

    override fun clearLatencies() = Unit

    override fun hasLatencies() = true

    override suspend fun getServerInfo(): VpnServerInfo = error("Not needed")
}