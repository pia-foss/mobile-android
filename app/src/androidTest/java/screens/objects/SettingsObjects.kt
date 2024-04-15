package screens.objects

import screens.helpers.UiAutomatorObjectFinder

object SettingsObjects {
    val protocolsButton =
        UiAutomatorObjectFinder.findByResourceId(":SettingsScreen:Protocols")

    fun navigateToSettingsPage()
    {
        MainScreenObjects.sideMenu.click()
        SideMenuObjects.settingsButton.click()
    }
}