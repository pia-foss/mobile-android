package screens.objects

import screens.objects.helpers.UiAutomatorObjectFinder

object SideMenuObjects {
    val settingsButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Settings")

    val dedicatedIP =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:DedicatedIP")

    val logoutButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:Logout")

    val logoutDialogueConfirmButton =
        UiAutomatorObjectFinder.findByResourceId(":SideMenu:ConfirmButton")
}
