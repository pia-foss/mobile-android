package screens.steps

import screens.objects.MainScreenObjects
import screens.steps.helpers.UiAutomatorStepsHelper
import screens.steps.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.CommonSteps
import kotlin.test.assertNotNull

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
