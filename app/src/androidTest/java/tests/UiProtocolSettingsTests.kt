package tests

import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiProtocolSettingsTests : UiTest() {

    @Test
    fun openVPN_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()
        protocolSteps.smallPacketToggleChecked(false)
        // press back twice to get back to the main screen
        UiAutomatorHelpers.pressBackTwice(2)

        mainScreenSteps.establishAndVerifyVPNConnection()

        assertNotNull(UiAutomatorHelpers.findByStartsWith("Protected"))
    }

    @Test
    fun openVPN_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()
        protocolSteps.smallPacketToggleChecked(true)
        // press back twice to get back to the main screen
        UiAutomatorHelpers.pressBackTwice(2)

        mainScreenSteps.establishAndVerifyVPNConnection()

        assertNotNull(UiAutomatorHelpers.findByStartsWith("Protected"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectWireGuard()
        protocolSteps.smallPacketToggleChecked(false)
        // press back twice to get back to the main screen
        UiAutomatorHelpers.pressBackTwice(2)

        mainScreenSteps.establishAndVerifyVPNConnection()

        assertNotNull(UiAutomatorHelpers.findByStartsWith("Protected"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectWireGuard()
        protocolSteps.smallPacketToggleChecked(true)
        // press back twice to get back to the main screen
        UiAutomatorHelpers.pressBackTwice(2)

        mainScreenSteps.establishAndVerifyVPNConnection()

        assertNotNull(UiAutomatorHelpers.findByStartsWith("Protected"))
    }
}
