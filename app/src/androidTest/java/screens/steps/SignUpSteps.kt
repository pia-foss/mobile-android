package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object SignUpSteps {
    val loginButton = UiAutomatorObjectFinder.findByResourceId(":SignUpScreen:Login")

    fun navigateToSignUpScreen() {
        try {
            UiAutomatorStepsHelper.waitUntilFound(loginButton)
        }
        catch (e: Exception) {
            UiAutomatorStepsHelper.waitUntilFound(MainScreenSteps.connectButton)
            MainScreenSteps.sideMenu.clickAndWaitForNewWindow(UiAutomatorStepsHelper.defaultTimeout)
            SideMenuSteps.logoutButton.clickAndWaitForNewWindow(UiAutomatorStepsHelper.defaultTimeout)
            SideMenuSteps.logoutDialogueConfirmButton.clickAndWaitForNewWindow(
                UiAutomatorStepsHelper.defaultTimeout
            )
            UiAutomatorStepsHelper.waitUntilFound(loginButton)
        }

        assert(loginButton.exists())
    }
}
