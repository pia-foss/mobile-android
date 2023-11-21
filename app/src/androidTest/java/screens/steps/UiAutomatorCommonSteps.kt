package screens.steps

import androidx.test.uiautomator.UiObjectNotFoundException
import screens.objects.MainScreenPageObjects
import screens.steps.helpers.UiAutomatorStepsHelper
import screens.steps.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.CommonSteps
import kotlin.test.assertNotNull

class UiAutomatorCommonSteps : CommonSteps {
    override fun navigateToMainScreen() {
        try {
            waitUntilFound(MainScreenPageObjects.connectButton)
        } catch (e: UiObjectNotFoundException) {
            UiAutomatorStepsHelper.device.pressBack()
        }

        assertNotNull(waitUntilFound(MainScreenPageObjects.connectButton))
    }
}