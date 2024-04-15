package screens.objects

import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object MainScreenObjects {
    val connectButton =
        UiAutomatorObjectFinder.findByResourceId(":ConnectionScreen:connection_button")
    val sideMenu =
        UiAutomatorObjectFinder.findByResourceId(":AppBar:side_menu")

    fun navigateToMainScreen()
    {
        try {
            UiAutomatorStepsHelper.waitUntilFound(connectButton)
        } catch (e: Exception) {
            UiAutomatorStepsHelper.device.pressBack()
            navigateToMainScreen()
        }
    }
}