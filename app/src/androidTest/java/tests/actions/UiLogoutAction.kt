package tests.actions

import screens.steps.interfaces.CommonSteps
import screens.steps.interfaces.SideMenuSteps

class UiLogoutAction(
    private val commonSteps: CommonSteps,
    private val sideMenuSteps: SideMenuSteps,
) {
    fun logout() {
        commonSteps.navigateToMainScreen()
        sideMenuSteps.clickOnSideMenu()
        sideMenuSteps.clickOnLogoutButton()
        sideMenuSteps.clickOnLogoutDialogueConfirmButton()
    }
}