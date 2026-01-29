package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.waitForStable
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.waitForElement

object SideMenuSteps {

    const val SETTINGS = ":SideMenu:Settings"
    const val DEDICATED_IP = ":SideMenu:DedicatedIP"
    const val LOGOUT = ":SideMenu:Logout"
    const val LOGOUT_DIALOGUE_CONFIRM_BUTTON = ":SideMenu:ConfirmButton"

    fun UiAutomatorTestScope.navigateToSettings() {
        navigateToSideMenu()
        waitForStableInActiveWindow()
        get(SETTINGS).click()
    }

    fun UiAutomatorTestScope.navigateToDedicatedIp() {
        navigateToSideMenu()
        waitForStableInActiveWindow()
        get(DEDICATED_IP).click()
        waitForStableInActiveWindow()
    }

    fun UiAutomatorTestScope.logout() {
        navigateToSideMenu()
        get(LOGOUT).click()
        get(LOGOUT_DIALOGUE_CONFIRM_BUTTON).click()
    }

    fun UiAutomatorTestScope.navigateToSideMenu() {
        get(MainScreenSteps.SIDE_MENU).click()
        waitForStableInActiveWindow()
    }
}
