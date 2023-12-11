package screens.steps

import screens.objects.SettingsObjects
import screens.steps.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.steps.interfaces.SettingsSteps

class UiAutomatorSettingsSteps : SettingsSteps {
    override fun clickOnProtocolsButton() {
        SettingsObjects.protocolsButton.clickAndWaitForNewWindow(defaultTimeout)
    }
}