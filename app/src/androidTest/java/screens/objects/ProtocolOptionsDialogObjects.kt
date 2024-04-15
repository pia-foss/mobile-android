package screens.objects

import screens.helpers.UiAutomatorObjectFinder

object ProtocolOptionsDialogObjects {
    val openVpnButton =
        UiAutomatorObjectFinder.findByResourceId(":OptionsDialog:OpenVPN")

    val androidOkButton = UiAutomatorObjectFinder.findByResourceId(":OptionsDialog:Ok")
}