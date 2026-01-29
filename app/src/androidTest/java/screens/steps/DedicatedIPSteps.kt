package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.inputText


object DedicatedIPSteps {

    const val DEDICATED_IP_FIELD = ":DedicatedIPScreen:dip_text_field"
    const val ACTIVATE_BUTTON = ":DedicatedIPScreen:activate_button"
    const val DEDICATED_IP_FLAG = ":DedicatedIPScreen:dip_flag"
    const val DEDICATED_IP_SERVER_NAME = ":DedicatedIPScreen:dip_server_name"

    fun UiAutomatorTestScope.activateDedicatedIPToken(token: String) {
        inputText(get(DEDICATED_IP_FIELD), token)
        get(ACTIVATE_BUTTON).click()
        waitForStableInActiveWindow()
    }
}
