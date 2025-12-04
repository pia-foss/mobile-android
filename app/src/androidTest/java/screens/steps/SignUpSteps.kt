package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible

object SignUpSteps {

    val loginButton get() = UiAutomatorHelpers.findByResId(":SignUpScreen:Login")

    fun navigateToSignUpScreen() {
        if (!waitUntilVisible(loginButton)) {
            MainScreenSteps.sideMenu?.let { UiAutomatorHelpers.click(it) }
            SideMenuSteps.logoutButton?.let { UiAutomatorHelpers.click(it) }
            SideMenuSteps.logoutDialogueConfirmButton?.let { UiAutomatorHelpers.click(it) }
            waitUntilVisible(loginButton)
        }
    }
}
