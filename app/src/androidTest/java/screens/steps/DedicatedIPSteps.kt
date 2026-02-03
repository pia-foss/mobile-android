package screens.steps

import screens.helpers.UiAutomatorHelpers


object DedicatedIPSteps {

    val dedicatedIPField= ":DedicatedIPScreen:dip_text_field"
    val activateButton=":DedicatedIPScreen:activate_button"
    val dedicatedIPFlag=":DedicatedIPScreen:dip_flag"
    val dedicatedIPServerName=":DedicatedIPScreen:dip_server_name"

    fun navigateToDedicatedIPPage() {
        UiAutomatorHelpers.clickWhenReady(MainScreenSteps.sideMenu)
        UiAutomatorHelpers.clickWhenReady(SideMenuSteps.dedicatedIP)
    }

    fun activateDedicatedIPToken(token: String) {
        UiAutomatorHelpers.inputTextWhenReady(dedicatedIPField, token)
        UiAutomatorHelpers.clickWhenReady(activateButton)
    }
}
