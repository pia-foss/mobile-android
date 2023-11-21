package screens.objects

import screens.objects.helpers.UiAutomatorObjectFinder

object MainScreenPageObjects {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")

    val sideMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:side_menu")
}