package screens.objects

import screens.objects.helpers.UiAutomatorObjectFinder

object SettingsObjects {
    val protocolsButton =
        UiAutomatorObjectFinder.findByResourceId(":SettingsScreen:Protocols")
}