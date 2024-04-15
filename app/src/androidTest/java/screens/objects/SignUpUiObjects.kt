package screens.objects

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object SignUpUiObjects {
    val loginButton = UiAutomatorObjectFinder.findByResourceId(":SignUpScreen:Login")

    fun navigateToSignUpScreen() {
        try {
            UiAutomatorStepsHelper.waitUntilFound(loginButton)
        }
        catch (e: Exception) {
            UiAutomatorStepsHelper.waitUntilFound(MainScreenObjects.connectButton)
            MainScreenObjects.sideMenu.clickAndWaitForNewWindow(UiAutomatorStepsHelper.defaultTimeout)
            SideMenuObjects.logoutButton.clickAndWaitForNewWindow(UiAutomatorStepsHelper.defaultTimeout)
            SideMenuObjects.logoutDialogueConfirmButton.clickAndWaitForNewWindow(
                UiAutomatorStepsHelper.defaultTimeout
            )
            UiAutomatorStepsHelper.waitUntilFound(loginButton)
        }

        assert(loginButton.exists())
    }
}
