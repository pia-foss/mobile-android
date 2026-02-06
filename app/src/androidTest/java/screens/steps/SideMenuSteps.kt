package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.findByResId

object SideMenuSteps {

    const val SETTINGS_BUTTON = ":SideMenu:Settings"
    const val DEDICATED_IP = ":SideMenu:DedicatedIP"
    const val LOGOUT_BUTTON = ":SideMenu:Logout"
    const val LOGOUT_DIALOG_CONFIRM_BUTTON = ":SideMenu:ConfirmButton"

    fun UiAutomatorTestScope.logOut() {
        findByResId(LOGOUT_BUTTON)?.click()
        findByResId(LOGOUT_DIALOG_CONFIRM_BUTTON)?.click()
    }

    fun UiAutomatorTestScope.navigateToSideMenu() {
        findByResId(MainScreenSteps.SIDE_MENU)?.click()
    }
}
