package screens.objects

import screens.objects.helpers.UiAutomatorObjectFinder

object ProtocolsObjects {
    val protocolSelectionButton =
        UiAutomatorObjectFinder.findByResourceId(":ProtocolSettingsScreen:protocol_selection")
}