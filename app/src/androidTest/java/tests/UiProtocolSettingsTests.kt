package tests

import androidx.test.uiautomator.uiAutomator
import org.junit.Before
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.pressBackTwice
import screens.steps.MainScreenSteps.establishAndVerifyVPNConnection
import screens.steps.ProtocolsSteps.selectOpenVPN
import screens.steps.ProtocolsSteps.selectProtocol
import screens.steps.ProtocolsSteps.selectWireGuard
import screens.steps.ProtocolsSteps.smallPacketToggleChecked
import screens.steps.SettingsSteps.navigateToSettingsPage

class UiProtocolSettingsTests : UiTest() {
    @Before
    fun setUp() {
        setupWithFreshLogin()
    }

    @Test
    fun openVPN_connectivity_when_small_packet_disabled() = uiAutomator {
        navigateToSettingsPage()
        selectProtocol()
        selectOpenVPN()
        smallPacketToggleChecked(false)
        // press back twice to get back to the main screen
        pressBackTwice(2)

        establishAndVerifyVPNConnection()
    }

    @Test
    fun openVPN_connectivity_when_small_packet_enabled() = uiAutomator {
        navigateToSettingsPage()
        selectProtocol()
        selectOpenVPN()
        smallPacketToggleChecked(true)
        // press back twice to get back to the main screen
        pressBackTwice(2)

        establishAndVerifyVPNConnection()
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_disabled() = uiAutomator {
        navigateToSettingsPage()
        selectProtocol()
        selectWireGuard()
        smallPacketToggleChecked(false)
        // press back twice to get back to the main screen
        pressBackTwice(2)

        establishAndVerifyVPNConnection()
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_enabled() = uiAutomator {
        navigateToSettingsPage()
        selectProtocol()
        selectWireGuard()
        smallPacketToggleChecked(true)
        // press back twice to get back to the main screen
        pressBackTwice(2)

        establishAndVerifyVPNConnection()
    }
}
