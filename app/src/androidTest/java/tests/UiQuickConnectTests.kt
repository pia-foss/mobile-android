package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.launchAppAndLogIn
import screens.helpers.UiAutomatorHelpers.waitForElement
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import screens.steps.MainScreenSteps
import screens.steps.MainScreenSteps.establishAndVerifyVPNConnection
import kotlin.test.assertTrue

class UiQuickConnectTests {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() = uiAutomator {
        launchAppAndLogIn()
        establishAndVerifyVPNConnection()
        get(MainScreenSteps.CONNECT_BUTTON).click()
        get(MainScreenSteps.QUICK_CONNECT_FIRST_ITEM).click()

        waitUntilConnectionIsEstablished()
        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }

    @Test
    fun quick_connect_should_switch_server_when_already_connected() = uiAutomator {
        launchAppAndLogIn()
        establishAndVerifyVPNConnection()
        get(MainScreenSteps.LOCATION_PICKER).click()
        waitForStableInActiveWindow()
        get(MainScreenSteps.LOCATION_EIGHT_ITEM).click()
        waitForElement(MainScreenSteps.QUICK_CONNECT_SECOND_ITEM)
        get(MainScreenSteps.QUICK_CONNECT_SECOND_ITEM).click()

        waitUntilConnectionIsEstablished()
        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }
}
