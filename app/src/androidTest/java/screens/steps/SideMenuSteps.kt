package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.click

object SideMenuSteps {

    const val SETTINGS_BUTTON = ":SideMenu:Settings"
    const val DEDICATED_IP = ":SideMenu:DedicatedIP"
    const val LOGOUT_BUTTON = ":SideMenu:Logout"
    const val LOGOUT_DIALOG_CONFIRM_BUTTON = ":SideMenu:ConfirmButton"

    fun UiAutomatorTestScope.logOut() {
        click(LOGOUT_BUTTON)
        click(LOGOUT_DIALOG_CONFIRM_BUTTON)
    }

    fun UiAutomatorTestScope.navigateToSideMenu() {
        click(MainScreenSteps.SIDE_MENU)
    }
}
