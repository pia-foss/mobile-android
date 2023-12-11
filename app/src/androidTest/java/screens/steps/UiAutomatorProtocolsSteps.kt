package screens.steps

import com.kape.settings.data.VpnProtocols
import screens.objects.ProtocolOptionsDialogObjects
import screens.objects.ProtocolsObjects
import screens.objects.SettingsObjects.protocolsButton
import screens.objects.helpers.UiAutomatorObjectFinder.findByText
import screens.steps.helpers.UiAutomatorStepsHelper.longTimeout
import screens.steps.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.ProtocolsSteps

class UiAutomatorProtocolsSteps : ProtocolsSteps {
    override fun clickOnProtocolSelectionButton() {
        ProtocolsObjects.protocolSelectionButton.clickAndWaitForNewWindow(longTimeout)
    }

    override fun clickOnOpenVpnButton() {
        ProtocolOptionsDialogObjects.openVpnButton.clickAndWaitForNewWindow(longTimeout)
        ProtocolOptionsDialogObjects.androidOkButton.clickAndWaitForNewWindow(longTimeout)
    }

    override fun getSelectedProtocol(): String {
        waitUntilFound(protocolsButton)
        return if (findByText(VpnProtocols.OpenVPN.name).exists())
            VpnProtocols.OpenVPN.name
        else if (findByText(VpnProtocols.WireGuard.name).exists())
            VpnProtocols.WireGuard.name
        else
            "Unknown protocol"
    }
}