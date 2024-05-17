package tests

import org.junit.Test
import kotlin.test.assertTrue

class UiProtocolSettingsTests : UiTest() {
    @Test
    fun openVPN_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()
        protocolSteps.smallPacketToggleChecked(false)
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.establishAndVerifyVPNConnection()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to"))
    }

    @Test
    fun openVPN_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()
        protocolSteps.smallPacketToggleChecked(true)
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.establishAndVerifyVPNConnection()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectWireGuard()
        protocolSteps.smallPacketToggleChecked(false)
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.establishAndVerifyVPNConnection()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to"))
    }

    @Test
    fun wireGuard_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectWireGuard()
        protocolSteps.smallPacketToggleChecked(true)
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.establishAndVerifyVPNConnection()
        val connectionText = mainScreenSteps.appBarConnectionStatus.text
        assertTrue(connectionText.contains("Connected to"))
    }
}
