package screens.steps

import screens.objects.MainScreenObjects
import screens.objects.SideMenuObjects
import screens.objects.SignUpUiObjects
import screens.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound
import screens.steps.interfaces.SideMenuSteps

class UiAutomatorSideMenuSteps : SideMenuSteps {
    override fun clickOnSideMenu() {
        MainScreenObjects.sideMenu.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnSettingsButton() {
        SideMenuObjects.settingsButton.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnDedicatedIP() {
        SideMenuObjects.dedicatedIP.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnLogoutButton() {
        SideMenuObjects.logoutButton.clickAndWaitForNewWindow(defaultTimeout)
    }

    override fun clickOnLogoutDialogueConfirmButton() {
        SideMenuObjects.logoutDialogueConfirmButton.clickAndWaitForNewWindow(defaultTimeout)
        waitUntilFound(SignUpUiObjects.loginButton)
    }}
