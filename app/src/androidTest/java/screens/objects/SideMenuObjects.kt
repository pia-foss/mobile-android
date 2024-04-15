package screens.objects

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object SideMenuObjects {
    val settingsButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Settings")
    val dedicatedIP =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:DedicatedIP")
    val logoutButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Logout")
    val logoutDialogueConfirmButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:ConfirmButton")

    fun logOut() {
        logoutButton.click()
        logoutDialogueConfirmButton.clickAndWaitForNewWindow()
        UiAutomatorStepsHelper.waitUntilFound(SignUpUiObjects.loginButton)
    }
}
