package screens.steps

import com.kape.settings.data.VpnProtocols
import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object ProtocolsSteps {
    val protocolSelectionButton =
        UiAutomatorObjectFinder.findByResourceId(":ProtocolSettingsScreen:protocol_selection")
    val openVpnButton =
        UiAutomatorObjectFinder.findByResourceId(":OptionsDialog:OpenVPN")

    val androidOkButton = UiAutomatorObjectFinder.findByResourceId(":OptionsDialog:Ok")

    fun selectOpenVPNProtocol(){
        SettingsSteps.protocolsButton.click()
        protocolSelectionButton.clickAndWaitForNewWindow()
        openVpnButton.clickAndWaitForNewWindow()
        androidOkButton.clickAndWaitForNewWindow()
    }

    fun getSelectedProtocol(): String {
        UiAutomatorStepsHelper.waitUntilFound(SettingsSteps.protocolsButton)
        return if (UiAutomatorObjectFinder.findByText(VpnProtocols.OpenVPN.name).exists())
            VpnProtocols.OpenVPN.name
        else if (UiAutomatorObjectFinder.findByText(VpnProtocols.WireGuard.name).exists())
            VpnProtocols.WireGuard.name
        else
            "Unknown protocol"
    }
}