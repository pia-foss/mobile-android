package screens.objects

import screens.helpers.UiAutomatorObjectFinder

object ProtocolsObjects {
    val protocolSelectionButton =
        UiAutomatorObjectFinder.findByResourceId(":ProtocolSettingsScreen:protocol_selection")
}