package screens.steps

import screens.objects.MainScreenObjects
import screens.helpers.UiAutomatorStepsHelper
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.CommonSteps

class UiAutomatorCommonSteps : CommonSteps {

    override fun navigateToMainScreen() {
        try {
            waitUntilFound(MainScreenObjects.connectButton)
        } catch (e: Exception) {
            UiAutomatorStepsHelper.device.pressBack()
            navigateToMainScreen()
        }
    }
}
