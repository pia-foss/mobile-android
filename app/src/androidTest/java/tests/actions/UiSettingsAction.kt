package tests.actions

import screens.steps.interfaces.ProtocolsSteps
import screens.steps.interfaces.SettingsSteps
import screens.steps.interfaces.SideMenuSteps

class UiSettingsAction(
    private val sideMenuSteps: SideMenuSteps,
    private val settingsSteps: SettingsSteps,
    private val protocolsSteps: ProtocolsSteps,
) {
    fun openSideMenu() {
        sideMenuSteps.clickOnSideMenu()
        sideMenuSteps.clickOnSettingsButton()
    }

    fun selectOpenVpnProtocol() {
        settingsSteps.clickOnProtocolsButton()
        protocolsSteps.clickOnProtocolSelectionButton()
        protocolsSteps.clickOnOpenVpnButton()
    }

    fun selectedProtocol(): String {
        return protocolsSteps.getSelectedProtocol()
    }
}