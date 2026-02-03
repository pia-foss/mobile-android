package screens.steps

import screens.helpers.UiAutomatorHelpers

object SideMenuSteps {

    val settingsButton=":SideMenu:Settings"
    val dedicatedIP=":SideMenu:DedicatedIP"
    val logoutButton=":SideMenu:Logout"
    val logoutDialogueConfirmButton=":SideMenu:ConfirmButton"

    fun logOut() {
        UiAutomatorHelpers.findByResId(logoutButton)?.click()
        UiAutomatorHelpers.findByResId(logoutDialogueConfirmButton)?.click()
    }

    fun navigateToSideMenu() {
        UiAutomatorHelpers.findByResId(MainScreenSteps.sideMenu)?.click()
    }
}
