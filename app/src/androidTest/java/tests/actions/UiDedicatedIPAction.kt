package tests.actions

import screens.steps.interfaces.DedicatedIPSteps
import screens.steps.interfaces.SideMenuSteps

class UiDedicatedIPAction(
    private val sideMenuSteps: SideMenuSteps,
    private val dedicatedIPSteps: DedicatedIPSteps
) {
    fun selectDedicatedIPFromSideMenu() {
        sideMenuSteps.clickOnSideMenu()
        sideMenuSteps.clickOnDedicatedIP()
    }

    fun activateDedicatedIP(dipToken: String) {
        dedicatedIPSteps.enterDedicatedIP(dipToken)
        dedicatedIPSteps.clickActivateButton()
    }
}
