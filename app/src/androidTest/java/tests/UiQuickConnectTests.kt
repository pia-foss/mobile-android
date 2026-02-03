package tests

import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import kotlin.test.assertTrue

class UiQuickConnectTests : UiTest() {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        UiAutomatorHelpers.findByResId(mainScreenSteps.connectButton)?.click()
        UiAutomatorHelpers.findByResId(mainScreenSteps.quickConnectFirstItem)?.click()

        waitUntilConnectionIsEstablished()
        val connectionText =
            UiAutomatorHelpers.findByResId(mainScreenSteps.appBarConnectionStatus)!!.text
        assertTrue(connectionText.startsWith("Protected"))
    }

    @Test
    fun quick_connect_should_switch_server_when_already_connected() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        UiAutomatorHelpers.findByResId(mainScreenSteps.locationPicker)?.click()
        UiAutomatorHelpers.findByResId(mainScreenSteps.locationEightItem)?.click()
        UiAutomatorHelpers.findByResId(mainScreenSteps.quickConnectSecondItem)?.click()

        waitUntilConnectionIsEstablished()
        val connectionText =
            UiAutomatorHelpers.findByResId(mainScreenSteps.appBarConnectionStatus)!!.text
        assertTrue(connectionText.startsWith("Protected"))
    }
}
