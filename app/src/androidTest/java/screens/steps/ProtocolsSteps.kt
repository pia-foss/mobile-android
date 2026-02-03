package screens.steps

import screens.helpers.UiAutomatorHelpers

object ProtocolsSteps {

    val protocolSelectionButton =":ProtocolSettingsScreen:protocol_selection"
    val openVpnButton =":OptionsDialog:OpenVPN"
    val wireGuardButton =":OptionsDialog:WireGuard"
    val androidOkButton =":OptionsDialog:Ok"
    val smallPacketsToggle =":ProtocolSettingsScreen:use_small_packets"

    fun selectProtocol() {
        UiAutomatorHelpers.clickWhenReady(SettingsSteps.protocolsButton)
        UiAutomatorHelpers.clickWhenReady(protocolSelectionButton)
    }

    fun selectOpenVPN() {
        UiAutomatorHelpers.clickWhenReady(openVpnButton)
        UiAutomatorHelpers.clickWhenReady(androidOkButton)
    }

    fun selectWireGuard() {
        UiAutomatorHelpers.clickWhenReady(wireGuardButton)
        UiAutomatorHelpers.clickWhenReady(androidOkButton)
    }

    fun smallPacketToggleChecked(desiredState: Boolean) {
        UiAutomatorHelpers.findByResId(smallPacketsToggle)?.let {
            if (it.isChecked != desiredState) {
                UiAutomatorHelpers.clickWhenReady(smallPacketsToggle)
            }
        }
    }
}
