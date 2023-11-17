package screens.steps

import screens.objects.MainScreenPageObjects
import screens.objects.SideMenuObjects

class UiAutomatorLogOutSteps: LogOutSteps {
    override fun goToSettings() {
        MainScreenPageObjects.hamburgerMenu.clickAndWaitForNewWindow(5000L)
    }

    override fun clickOnLogoutSettingsButton() {
        SideMenuObjects.logOutButton.clickAndWaitForNewWindow(5000L)
    }
}