package com.kape.splash.ui

import com.kape.contracts.ConnectionManager
import com.kape.data.vpnserver.VpnServer
import com.kape.splash.domain.AppRelease
import com.kape.splash.domain.AppUpdateRepository
import com.kape.splash.domain.usecase.GetUpdateUrlUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpdateViewModelTest {
    private val events = mutableListOf<String>()
    private val vpn = FakeVpn(events)
    private val updates =
        object : AppUpdateRepository {
            override suspend fun getLatestRelease() = AppRelease("https://example.com/latest.apk", versionCode = 1)
        }
    private val viewModel = UpdateViewModel(GetUpdateUrlUseCase(updates, storeUpdateUrl = ""), vpn)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `disconnects a connected VPN before opening the update page`() {
        vpn.connected = true

        viewModel.onUpdateClicked { events += "open $it" }

        assertEquals(listOf("disconnect", "open https://example.com/latest.apk"), events)
    }

    @Test
    fun `disconnects a VPN that is still connecting`() {
        vpn.connecting = true

        viewModel.onUpdateClicked { events += "open $it" }

        assertEquals(listOf("disconnect", "open https://example.com/latest.apk"), events)
    }

    @Test
    fun `opens the update page without disconnecting when the VPN is off`() {
        viewModel.onUpdateClicked { events += "open $it" }

        assertEquals(listOf("open https://example.com/latest.apk"), events)
    }
}

private class FakeVpn(
    private val events: MutableList<String>,
) : ConnectionManager {
    var connected = false
    var connecting = false

    override fun isConnected() = connected

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    ) = Unit

    override suspend fun connectToLastKnownOrOptimalServer() = Unit

    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) = Unit

    override fun isConnectionInProgress() = connecting

    override suspend fun disconnect() {
        events += "disconnect"
        connected = false
        connecting = false
    }
}