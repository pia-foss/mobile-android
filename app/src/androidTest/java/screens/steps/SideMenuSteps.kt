package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible

object SideMenuSteps {

    val settingsButton get() = UiAutomatorHelpers.findByResId(":SideMenu:Settings")
    val dedicatedIP get() = UiAutomatorHelpers.findByResId(":SideMenu:DedicatedIP")
    val logoutButton get() = UiAutomatorHelpers.findByResId(":SideMenu:Logout")
    val logoutDialogueConfirmButton get() = UiAutomatorHelpers.findByResId(":SideMenu:ConfirmButton")

    fun logOut() {
        UiAutomatorHelpers.click(logoutButton)
        UiAutomatorHelpers.click(logoutDialogueConfirmButton)
        waitUntilVisible(SignUpSteps.loginButton)
    }

    fun navigateToSideMenu() {
        MainScreenSteps.navigateToMainScreen()
        UiAutomatorHelpers.click(MainScreenSteps.sideMenu)
    }
}
