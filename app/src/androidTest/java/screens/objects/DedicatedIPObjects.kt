package screens.objects

import screens.helpers.UiAutomatorObjectFinder

object DedicatedIPObjects {

    val dedicatedIPField = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_text_field")
    val activateButton = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:activate_button")
    val dedicatedIPFlag = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_flag")
    val dedicatedIPServerName = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_server_name")
}
