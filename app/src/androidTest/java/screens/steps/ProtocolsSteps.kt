package screens.steps

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.Until
import screens.helpers.UiAutomatorHelpers.DEFAULT_TIMEOUT
import screens.helpers.UiAutomatorHelpers.click
import screens.helpers.UiAutomatorHelpers.findByResId

object ProtocolsSteps {

    const val PROTOCOL_SELECTION_BUTTON = ":ProtocolSettingsScreen:protocol_selection"
    const val OPEN_VPN_BUTTON = ":OptionsDialog:OpenVPN"
    const val WIRE_GUARD_BUTTON = ":OptionsDialog:WireGuard"
    const val ANDROID_OK_BUTTON = ":OptionsDialog:Ok"
    const val SMALL_PACKETS_TOGGLE = ":ProtocolSettingsScreen:use_small_packets"

    fun UiAutomatorTestScope.selectProtocol() {
        click(SettingsSteps.PROTOCOLS_BUTTON)
        click(PROTOCOL_SELECTION_BUTTON)
    }

    fun UiAutomatorTestScope.selectOpenVPN() {
        click(OPEN_VPN_BUTTON)
        click(ANDROID_OK_BUTTON)
        device.wait(Until.gone(By.res(ANDROID_OK_BUTTON)), DEFAULT_TIMEOUT)
    }

    fun UiAutomatorTestScope.selectWireGuard() {
        click(WIRE_GUARD_BUTTON)
        click(ANDROID_OK_BUTTON)
        device.wait(Until.gone(By.res(ANDROID_OK_BUTTON)), DEFAULT_TIMEOUT)
    }

    fun UiAutomatorTestScope.smallPacketToggleChecked(desiredState: Boolean) {
        val toggle = findByResId(SMALL_PACKETS_TOGGLE)
            ?: error("Small packets toggle not found")
        if (toggle.isChecked != desiredState) {
            toggle.click()
            device.waitForIdle()
        }
    }
}
