package com.kape.obfuscationregionselection.ui.vm

import com.kape.contracts.Router
import com.kape.data.RegionItemType
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.api.ShadowsocksRegionStore
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import com.kape.regions.shadowsocks.domain.usecase.ObserveShadowsocksServersUseCase
import com.kape.regions.shadowsocks.domain.usecase.RefreshShadowsocksServersUseCase
import com.kape.regions.shadowsocks.domain.usecase.SelectShadowsocksServerUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShadowsocksRegionSelectionViewModelTest {
    private val berlin = server("Berlin")
    private val paris = server("Paris")
    private val repository = FakeShadowsocksRepository(listOf(berlin, paris))
    private val favorites = MutableStateFlow(emptyList<String>())
    private val prefs = mockk<ShadowsocksRegionStore>(relaxed = true) { every { observeFavorites() } returns favorites }
    private val router = mockk<Router>(relaxed = true)

    private lateinit var viewModel: ShadowsocksRegionSelectionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            ShadowsocksRegionSelectionViewModel(
                ObserveShadowsocksServersUseCase(repository),
                RefreshShadowsocksServersUseCase(repository),
                SelectShadowsocksServerUseCase(repository),
                prefs,
                router,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun rows() =
        viewModel.state.value.visibleRegions.map { item ->
            when (val type = item.type) {
                is RegionItemType.Content -> type.server.region + if (type.isFavorite) "*" else ""
                RegionItemType.HeadingFavorites -> "[favorites]"
                RegionItemType.HeadingAll -> "[all]"
            }
        }

    @Test
    fun `favorites are listed on top and not repeated below`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            favorites.value = listOf("Paris")

            assertEquals(listOf("[favorites]", "Paris*", "[all]", "Berlin"), rows())
        }

    @Test
    fun `searching lists the matching regions`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            viewModel.search("par")

            assertEquals(listOf("Paris"), rows())
        }

    @Test
    fun `picking a region saves it before closing the screen`() {
        viewModel.onRegionClicked(paris)

        assertEquals(paris, repository.selected)
        verify(exactly = 1) { router.navigateBack() }
    }

    @Test
    fun `the favorite toggle adds a region that isn't a favorite`() {
        every { prefs.isFavorite("Paris") } returns flowOf(false)

        viewModel.onFavoriteClicked(paris)

        coVerify { prefs.addToFavorites("Paris") }
    }
}

private fun server(region: String) = ShadowsocksServer(region = region, host = "$region.host", port = 443, key = "key", cipher = "aes")

private class FakeShadowsocksRepository(
    servers: List<ShadowsocksServer>,
) : ShadowsocksRegionRepository {
    private val servers = MutableStateFlow(servers)
    var selected: ShadowsocksServer? = null

    override fun observeServers() = servers

    override suspend fun refreshServers(locale: String) = Unit

    override fun observeSelectedServer() = MutableStateFlow(selected)

    override suspend fun selectServer(server: ShadowsocksServer) {
        selected = server
    }
}