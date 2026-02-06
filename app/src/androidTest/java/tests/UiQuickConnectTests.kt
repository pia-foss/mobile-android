package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.helpers.UiAutomatorHelpers.findByStartsWith
import screens.helpers.UiAutomatorHelpers.waitUntilConnectionIsEstablished
import screens.steps.MainScreenSteps
import screens.steps.MainScreenSteps.establishAndVerifyVPNConnection
import kotlin.test.assertNotNull

class UiQuickConnectTests : UiTest() {

    @Test
    fun quick_connect_should_connect_from_disconnect_state() = uiAutomator {
        establishAndVerifyVPNConnection()
        findByResId(MainScreenSteps.CONNECT_BUTTON)?.click()
        findByResId(MainScreenSteps.QUICK_CONNECT_FIRST_ITEM)?.click()

        waitUntilConnectionIsEstablished()
        assertNotNull(findByStartsWith("Protected"))
    }

    @Test
    fun quick_connect_should_switch_server_when_already_connected() = uiAutomator {
        establishAndVerifyVPNConnection()
        findByResId(MainScreenSteps.LOCATION_PICKER)?.click()
        findByResId(MainScreenSteps.LOCATION_EIGHT_ITEM)?.click()
        findByResId(MainScreenSteps.QUICK_CONNECT_SECOND_ITEM)?.click()

        waitUntilConnectionIsEstablished()
        assertNotNull(findByStartsWith("Protected"))
    }
}
