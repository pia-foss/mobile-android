package screens.steps

import screens.objects.DedicatedIPObjects
import screens.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.helpers.UiAutomatorStepsHelper.inputTextInField
import screens.steps.interfaces.DedicatedIPSteps

class UiAutomatorDedicatedIPSteps : DedicatedIPSteps  {

    override fun enterDedicatedIP(dip: String) {
        inputTextInField(DedicatedIPObjects.dedicatedIPField, dip)
    }

    override fun clickActivateButton() {
        DedicatedIPObjects.activateButton.clickAndWaitForNewWindow(defaultTimeout)
    }
}
