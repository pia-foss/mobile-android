package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import com.kape.settings.data.VpnProtocols
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.waitForElement

object ProtocolsSteps {

    const val PROTOCOL_SELECTION_BUTTON = ":ProtocolSettingsScreen:protocol_selection"
    const val OPEN_VPN_BUTTON = ":OptionsDialog:OpenVPN"
    const val WIRE_GUARD_BUTTON = ":OptionsDialog:WireGuard"
    const val ANDROID_OK_BUTTON = ":OptionsDialog:Ok"
    const val SMALL_PACKETS_TOGGLE = ":ProtocolSettingsScreen:use_small_packets"

    fun UiAutomatorTestScope.selectProtocol() {
        waitForStableInActiveWindow()
        get(SettingsSteps.PROTOCOLS_BUTTON).click()
        get(PROTOCOL_SELECTION_BUTTON).click()
    }

    fun UiAutomatorTestScope.selectOpenVPN() {
        waitForStableInActiveWindow()
        get(OPEN_VPN_BUTTON).click()
        get(ANDROID_OK_BUTTON).click()
    }

    fun UiAutomatorTestScope.selectWireGuard() {
        waitForStableInActiveWindow()
        get(WIRE_GUARD_BUTTON).click()
        get(ANDROID_OK_BUTTON).click()
    }

    fun UiAutomatorTestScope.getSelectedProtocol(): String {
        waitForElement(SettingsSteps.PROTOCOLS_BUTTON)
        return when {
            onElement { viewIdResourceName == SettingsSteps.PROTOCOLS_BUTTON }.text.contains(VpnProtocols.OpenVPN.name) -> VpnProtocols.OpenVPN.name
            onElement { viewIdResourceName == SettingsSteps.PROTOCOLS_BUTTON }.text.contains(VpnProtocols.WireGuard.name) -> VpnProtocols.WireGuard.name
            else -> "Unknown protocol"
        }
    }

    fun UiAutomatorTestScope.smallPacketToggleChecked(desiredState: Boolean) {
        if (get(SMALL_PACKETS_TOGGLE).isChecked != desiredState) {
            get(SMALL_PACKETS_TOGGLE).click()
        }
    }
}
