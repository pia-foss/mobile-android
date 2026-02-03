package screens.steps

import screens.helpers.UiAutomatorHelpers

object SettingsSteps {
    val protocolsButton = ":SettingsScreen:Protocols"

    fun navigateToSettingsPage() {
        UiAutomatorHelpers.clickWhenReady(MainScreenSteps.sideMenu)
        UiAutomatorHelpers.clickWhenReady(SideMenuSteps.settingsButton)
    }
}
