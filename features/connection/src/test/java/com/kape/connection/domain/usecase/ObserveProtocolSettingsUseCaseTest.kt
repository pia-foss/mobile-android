package com.kape.connection.domain.usecase

import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveProtocolSettingsUseCaseTest {
    private val protocol = MutableStateFlow(VpnProtocols.WireGuard)
    private val wireGuard = MutableStateFlow(WireGuardSettings())
    private val openVpn = MutableStateFlow(OpenVpnSettings())
    private val automatic = MutableStateFlow(AutomaticSettings())
    private val settingsPrefs =
        mockk<SettingsStore> {
            every { selectedProtocol } returns protocol
            every { wireGuardSettings } returns wireGuard
            every { openVpnSettings } returns openVpn
            every { autoSettings } returns automatic
        }

    @Test
    fun `follows the settings of the currently selected protocol`() =
        runTest {
            val emitted = mutableListOf<ProtocolSettings>()
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler),
            ) { ObserveProtocolSettingsUseCase(settingsPrefs)().toList(emitted) }

            protocol.value = VpnProtocols.OpenVPN
            openVpn.value = OpenVpnSettings(port = "53")
            protocol.value = VpnProtocols.Automatic
            openVpn.value = OpenVpnSettings(port = "443")

            assertEquals(
                listOf(WireGuardSettings(), OpenVpnSettings(), OpenVpnSettings(port = "53"), AutomaticSettings()),
                emitted,
            )
        }
}