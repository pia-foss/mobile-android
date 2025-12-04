package screens.steps

import screens.helpers.UiAutomatorHelpers

object SettingsSteps {
    val protocolsButton get() = UiAutomatorHelpers.findByResId(":SettingsScreen:Protocols")

    fun navigateToSettingsPage() {
        UiAutomatorHelpers.click(MainScreenSteps.sideMenu)
        UiAutomatorHelpers.click(SideMenuSteps.settingsButton)
    }
}
