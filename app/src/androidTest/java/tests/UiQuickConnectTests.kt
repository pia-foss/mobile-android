package tests

import org.junit.Test
import kotlin.test.assertTrue

class UiQuickConnectTests : UiTest() {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        mainScreenSteps.connectButton.click() //disconnect
        mainScreenSteps.quickConnectServer.click()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to") &&
                mainScreenSteps.quickConnectServerText.exists())
    }
}
