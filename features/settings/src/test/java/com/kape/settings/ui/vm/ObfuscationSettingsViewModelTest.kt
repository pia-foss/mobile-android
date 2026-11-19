package com.kape.settings.ui.vm

import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObfuscationSettingsViewModelTest {
    private val proxyEnabled = MutableStateFlow(false)
    private val openVpn = MutableStateFlow(OpenVpnSettings(transport = Transport.UDP))
    private lateinit var viewModel: ObfuscationSettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val prefs =
            mockk<SettingsStore>(relaxed = true) {
                every { isExternalProxyAppEnabled } returns proxyEnabled
                every { openVpnSettings } returns openVpn
            }
        viewModel =
            ObfuscationSettingsViewModel(
                prefs = prefs,
                connectionPrefs = mockk<ConnectionStore>(relaxed = true),
                setTransportUseCase = mockk(relaxed = true),
                setAllowLocalNetwork = mockk(relaxed = true),
                isNumericIp = mockk(relaxed = true),
                router = mockk(relaxed = true),
                reconnectPrompt = mockk(relaxed = true),
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a proxy app over UDP asks to switch to TCP, and stops asking once on TCP`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.showExternalProxyTcpPrompt.collect {} }
            assertFalse(viewModel.showExternalProxyTcpPrompt.value)

            proxyEnabled.value = true
            assertTrue(viewModel.showExternalProxyTcpPrompt.value)

            openVpn.value = OpenVpnSettings(transport = Transport.TCP)
            assertFalse(viewModel.showExternalProxyTcpPrompt.value)
        }

    @Test
    fun `ports must be numbers from 1 to 65535`() {
        assertEquals(
            listOf(true, true, false, false, false, false),
            listOf("1", "65535", "0", "65536", "", "abc").map(viewModel::isPortValid),
        )
    }
}