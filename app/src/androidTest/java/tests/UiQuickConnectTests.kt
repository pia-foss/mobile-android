package tests

import org.junit.Test
import screens.helpers.UiAutomatorStepsHelper.defaultTimeout
import screens.helpers.UiAutomatorStepsHelper.waitUntilConnectionIsEstablished
import kotlin.test.assertTrue

class UiQuickConnectTests : UiTest() {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        mainScreenSteps.connectButton.click()
        mainScreenSteps.quickConnectFirstItem.click()
        waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to") && mainScreenSteps.quickConnectFirstItem.exists())
    }

    @Test
    fun quick_connect_should_switch_server_when_already_connected() {
        mainScreenSteps.establishAndVerifyVPNConnection()
        mainScreenSteps.locationPicker.clickAndWaitForNewWindow(defaultTimeout)
        mainScreenSteps.locationEightItem.clickAndWaitForNewWindow(defaultTimeout)
        mainScreenSteps.quickConnectSecondItem.click()
        waitUntilConnectionIsEstablished()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to") && mainScreenSteps.quickConnectSecondItem.exists())
    }
}
