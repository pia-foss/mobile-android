package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.launchAppAndLogIn
import screens.steps.MainScreenSteps
import screens.steps.MainScreenSteps.establishAndVerifyVPNConnection
import screens.steps.ProtocolsSteps.selectOpenVPN
import screens.steps.ProtocolsSteps.selectProtocol
import screens.steps.ProtocolsSteps.selectWireGuard
import screens.steps.ProtocolsSteps.smallPacketToggleChecked
import screens.steps.SideMenuSteps.navigateToSettings
import kotlin.test.assertTrue

class UiProtocolSettingsTests {

    @Test
    fun openVPN_connectivity_when_small_packet_disabled() = uiAutomator {
        launchAppAndLogIn()
        navigateToSettings()
        selectProtocol()
        selectOpenVPN()
        smallPacketToggleChecked(false)
        // get back to main screen
        pressBack()
        pressBack()
        establishAndVerifyVPNConnection()

        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }

    @Test
    fun openVPN_connectivity_when_small_packet_enabled() = uiAutomator {
        launchAppAndLogIn()
        navigateToSettings()
        selectProtocol()
        selectOpenVPN()
        smallPacketToggleChecked(true)
        // get back to main screen
        pressBack()
        pressBack()
        establishAndVerifyVPNConnection()

        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_disabled() = uiAutomator {
        launchAppAndLogIn()
        navigateToSettings()
        selectProtocol()
        selectWireGuard()
        smallPacketToggleChecked(false)
        // get back to main screen
        pressBack()
        pressBack()
        establishAndVerifyVPNConnection()

        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_enabled() = uiAutomator {
        launchAppAndLogIn()
        navigateToSettings()
        selectProtocol()
        selectWireGuard()
        smallPacketToggleChecked(true)
        // get back to main screen
        pressBack()
        pressBack()
        establishAndVerifyVPNConnection()

        val connectionText = get(MainScreenSteps.APPBAR_CONNECTION_STATUS).text
        assertTrue(connectionText.startsWith("Protected"))
    }
}
