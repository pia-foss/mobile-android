package screens.steps

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object SideMenuSteps {
    val settingsButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Settings")
    val dedicatedIP =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:DedicatedIP")
    val logoutButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Logout")
    val logoutDialogueConfirmButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:ConfirmButton")

    fun logOut() {
        logoutButton.clickAndWaitForNewWindow()
        logoutDialogueConfirmButton.clickAndWaitForNewWindow()
        UiAutomatorStepsHelper.waitUntilFound(SignUpSteps.loginButton)
    }

    fun navigateToSideMenu() {
        MainScreenSteps.navigateToMainScreen()
        MainScreenSteps.sideMenu.click()
    }
}
