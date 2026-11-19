package com.kape.vpnregionselection.ui.vm

import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.Router
import com.kape.data.RegionItemType
import com.kape.data.RegionServerItem
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.vpnregionselection.domain.usecase.ConnectToVpnRegionUseCase
import com.kape.vpnregionselection.domain.usecase.ObserveVpnRegionsUseCase
import com.kape.vpnregionselection.vpnServer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class VpnRegionSelectionViewModelTest {
    private val de = vpnServer("de")
    private val fr = vpnServer("fr")
    private var connected = false

    private val connectToRegion = mockk<ConnectToVpnRegionUseCase> { every { this@mockk(any()) } returns Job() }
    private val router = mockk<Router>(relaxed = true)
    private val connectionInfo = mockk<ConnectionInfoProvider> { every { isConnected() } answers { connected } }
    private val connectionPrefs = mockk<ConnectionStore> { coEvery { getSelectedVpnServerNow() } returns de }
    private val observeRegions =
        mockk<ObserveVpnRegionsUseCase> {
            every { this@mockk() } returns
                flowOf(listOf(de, fr).map { RegionServerItem(RegionItemType.Content(isFavorite = false, server = it)) })
        }

    private lateinit var viewModel: VpnRegionSelectionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            VpnRegionSelectionViewModel(
                observeVpnRegions = observeRegions,
                refreshVpnServers = mockk(relaxed = true),
                toggleFavoriteVpnRegion = mockk(relaxed = true),
                connectToVpnRegion = connectToRegion,
                connectionInfo = connectionInfo,
                connectionPrefs = connectionPrefs,
                router = router,
                settingsPrefs = mockk { every { isPortForwardingEnabled } returns MutableStateFlow(false) },
                appUpdateStatus = mockk<AppUpdateStatus> { every { hasUpdateAvailable } returns MutableStateFlow(false) },
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `while disconnected, picking a region connects to it and closes the screen`() {
        viewModel.onRegionClicked(fr)

        verify(exactly = 1) { connectToRegion(fr) }
        verify(exactly = 1) { router.navigateBack() }
    }

    @Test
    fun `picking the region already connected to only closes the screen`() {
        connected = true

        viewModel.onRegionClicked(de)

        verify(exactly = 0) { connectToRegion(any()) }
        verify(exactly = 1) { router.navigateBack() }
    }

    @Test
    fun `while connected elsewhere, switching waits for the user to confirm`() {
        connected = true

        viewModel.onRegionClicked(fr)

        assertEquals(fr, viewModel.pendingLocationChange.value)
        verify(exactly = 0) { connectToRegion(any()) }

        viewModel.onLocationChangeConfirmed()

        assertNull(viewModel.pendingLocationChange.value)
        verify(exactly = 1) { connectToRegion(fr) }
        verify(exactly = 1) { router.navigateBack() }
    }

    @Test
    fun `dismissing the location change keeps the connection`() {
        connected = true
        viewModel.onRegionClicked(fr)

        viewModel.onLocationChangeDismissed()

        assertNull(viewModel.pendingLocationChange.value)
        verify(exactly = 0) { connectToRegion(any()) }
        verify(exactly = 0) { router.navigateBack() }
    }

    @Test
    fun `searching lists only the matching regions`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            viewModel.search("FR")

            val results =
                viewModel.state.value.searchResults
                    ?.map { (it.type as RegionItemType.Content<VpnServer>).server }
            assertEquals(listOf(fr), results)
        }

    @Test
    fun `clearing the search shows every region again`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
            viewModel.search("FR")

            viewModel.search("")

            assertNull(viewModel.state.value.searchResults)
            assertEquals(2, viewModel.state.value.visibleRegions.size)
        }
}