package com.kape.vpnregionselection.ui.vm

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.utils.UpdateAvailableManager
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregionselection.util.ItemType
import com.kape.vpnregionselection.util.ServerItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VpnRegionSelectionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val router: Router = mockk(relaxed = true)
    private val regionListProvider: RegionListProvider = mockk(relaxed = true)
    private val vpnRegionPrefs: VpnRegionPrefs = mockk()
    private val settingsPrefs: SettingsPrefs = mockk()
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val connectionInfoProvider: ConnectionInfoProvider = mockk(relaxed = true)
    private val connectionManager: ConnectionManager = mockk(relaxed = true)
    private val updateAvailableManager: UpdateAvailableManager = mockk(relaxed = true)

    private lateinit var viewModel: VpnRegionSelectionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { settingsPrefs.isShowGeoLocatedServersEnabled } returns MutableStateFlow(false)
        every { settingsPrefs.selectedProtocol } returns MutableStateFlow(VpnProtocols.WireGuard)
        every { settingsPrefs.isPortForwardingEnabled } returns MutableStateFlow(false)
        every { vpnRegionPrefs.isFavorite(any()) } returns flowOf(false)
        every { updateAvailableManager.hasUpdateAvailable } returns MutableStateFlow(false)

        viewModel =
            VpnRegionSelectionViewModel(
                router,
                regionListProvider,
                vpnRegionPrefs,
                settingsPrefs,
                connectionPrefs,
                connectionInfoProvider,
                connectionManager,
                updateAvailableManager,
                testDispatcher,
            )
        viewModel.autoRegionName = ""
        viewModel.autoRegionIso = ""
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test a search typed before the server list is populated is re-applied once it arrives`() =
        runTest {
            // Search while `servers` is still empty - e.g. right after a failed region fetch,
            // before the fallback/retry has populated anything yet.
            viewModel.filterByName("norway")
            assertEquals(emptyList(), viewModel.sorted.value)

            // The list is (re)built later, independently of the search - e.g. once the region
            // fetch succeeds or the asset fallback kicks in.
            viewModel.arrangeVpnServers(listOf(norway, singapore))

            val matched = viewModel.sorted.value.filterIsInstance<ServerItem>()
            assertTrue(matched.any { (it.type as? ItemType.Content)?.server?.name == "Norway" })
            assertTrue(matched.none { (it.type as? ItemType.Content)?.server?.name == "Singapore" })
        }

    @Test
    fun `test filterByName narrows the already-populated list immediately`() =
        runTest {
            viewModel.arrangeVpnServers(listOf(norway, singapore))

            viewModel.filterByName("singapore")

            val matched = viewModel.sorted.value.mapNotNull { (it.type as? ItemType.Content)?.server?.name }
            assertEquals(listOf("Singapore"), matched)
        }

    @Test
    fun `test clearing the search empties the results`() =
        runTest {
            viewModel.arrangeVpnServers(listOf(norway, singapore))
            viewModel.filterByName("norway")

            viewModel.filterByName("")

            assertEquals(emptyList(), viewModel.sorted.value)
        }

    companion object {
        private fun server(name: String) =
            VpnServer(
                name = name,
                iso = name.take(2).lowercase(),
                dns = "",
                latency = null,
                endpoints = mapOf(VpnServer.ServerGroup.WIREGUARD to listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "cn"))),
                key = name.lowercase(),
                latitude = null,
                longitude = null,
                isGeo = false,
                isOffline = false,
                allowsPortForwarding = false,
                autoRegion = false,
                dipToken = null,
                dedicatedIp = null,
            )

        private val norway = server("Norway")
        private val singapore = server("Singapore")
    }
}