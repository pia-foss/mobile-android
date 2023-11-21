package screens.steps

import screens.objects.MainScreenPageObjects
import screens.objects.SideMenuObjects
import screens.steps.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.steps.interfaces.SideMenuSteps

class UiAutomatorSideMenuSteps : SideMenuSteps {
    override fun goToSettings() {
        MainScreenPageObjects.sideMenu.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnSideMenuSettingsButton() {
        SideMenuObjects.settingsButton.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnSideMenuLogoutButton() {
        SideMenuObjects.logOutButton.clickAndWaitForNewWindow(defaultTimeout)
    }
}