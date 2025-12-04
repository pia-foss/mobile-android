package screens.steps

import screens.helpers.UiAutomatorHelpers


object DedicatedIPSteps {

    val dedicatedIPField get() = UiAutomatorHelpers.findByResId(":DedicatedIPScreen:dip_text_field")
    val activateButton get() = UiAutomatorHelpers.findByResId(":DedicatedIPScreen:activate_button")
    val dedicatedIPFlag get() = UiAutomatorHelpers.findByResId(":DedicatedIPScreen:dip_flag")
    val dedicatedIPServerName get() = UiAutomatorHelpers.findByResId(":DedicatedIPScreen:dip_server_name")

    fun navigateToDedicatedIPPage() {
        UiAutomatorHelpers.click(MainScreenSteps.sideMenu)
        UiAutomatorHelpers.click(SideMenuSteps.dedicatedIP)
    }

    fun activateDedicatedIPToken(token: String) {
        dedicatedIPField?.let { UiAutomatorHelpers.inputText(it, token) }
        UiAutomatorHelpers.click(activateButton)
    }
}
