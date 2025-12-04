package screens.steps

import com.kape.settings.data.VpnProtocols
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible

object ProtocolsSteps {

    val protocolSelectionButton get() = UiAutomatorHelpers.findByResId(":ProtocolSettingsScreen:protocol_selection")
    val openVpnButton get() = UiAutomatorHelpers.findByResId(":OptionsDialog:OpenVPN")
    val wireGuardButton get() = UiAutomatorHelpers.findByResId(":OptionsDialog:WireGuard")
    val androidOkButton get() = UiAutomatorHelpers.findByResId(":OptionsDialog:Ok")
    val smallPacketsToggle get() = UiAutomatorHelpers.findByResId(":ProtocolSettingsScreen:use_small_packets")

    fun selectProtocol() {
        UiAutomatorHelpers.click(SettingsSteps.protocolsButton)
        UiAutomatorHelpers.click(protocolSelectionButton)
    }

    fun selectOpenVPN() {
        UiAutomatorHelpers.click(openVpnButton)
        UiAutomatorHelpers.click(androidOkButton)
    }

    fun selectWireGuard() {
        UiAutomatorHelpers.click(wireGuardButton)
        UiAutomatorHelpers.click(androidOkButton)
    }

    fun getSelectedProtocol(): String {
        waitUntilVisible(SettingsSteps.protocolsButton)
        return when {
            UiAutomatorHelpers.findByText(VpnProtocols.OpenVPN.name)?.let { it.isEnabled } == true -> VpnProtocols.OpenVPN.name
            UiAutomatorHelpers.findByText(VpnProtocols.WireGuard.name)?.let { it.isEnabled } == true -> VpnProtocols.WireGuard.name
            else -> "Unknown protocol"
        }
    }

    fun smallPacketToggleChecked(desiredState: Boolean) {
        smallPacketsToggle?.let {
            if (it.isChecked != desiredState) {
                UiAutomatorHelpers.click(it)
            }
        }
    }
}
