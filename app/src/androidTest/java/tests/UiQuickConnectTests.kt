package tests

import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import screens.helpers.UiAutomatorHelpers.waitUntilVisible
import kotlin.test.assertTrue

class UiQuickConnectTests : UiTest() {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        mainScreenSteps.connectButton?.click()
        mainScreenSteps.quickConnectFirstItem?.let { UiAutomatorHelpers.clickAndWaitForNewWindow(it) }

        waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus?.text?:""
        assertTrue(connectionText.contains("Protected"))
    }

    @Test
    fun quick_connect_should_switch_server_when_already_connected() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        mainScreenSteps.locationPicker?.let { UiAutomatorHelpers.clickAndWaitForNewWindow(it) }
        mainScreenSteps.locationEightItem?.let { UiAutomatorHelpers.clickAndWaitForNewWindow(it) }
        mainScreenSteps.quickConnectSecondItem?.let { UiAutomatorHelpers.clickAndWaitForNewWindow(it) }

        waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus?.text?:""
        assertTrue(connectionText.contains("Protected"))
    }
}
