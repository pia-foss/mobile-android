package screens.steps

import screens.helpers.UiAutomatorObjectFinder

object SettingsSteps {
    val protocolsButton =
        UiAutomatorObjectFinder.findByResourceId(":SettingsScreen:Protocols")

    fun navigateToSettingsPage()
    {
        MainScreenSteps.sideMenu.click()
        SideMenuSteps.settingsButton.click()
    }
}
