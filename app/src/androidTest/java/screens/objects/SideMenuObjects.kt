package screens.objects

import screens.objects.helpers.UiAutomatorObjectFinder

object SideMenuObjects {
    val settingsButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Settings")

    val logOutButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:LogOut")
}