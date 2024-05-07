package tests

import org.junit.Test
import screens.helpers.UiAutomatorStepsHelper.waitUntilTextFound
import org.junit.Assert.fail
import org.junit.Ignore
import kotlin.test.assertTrue


class ProtocolSettingsTests : UiTest() {

    @Test
    fun openVPN_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage() //select settings
        protocolSteps.selectProtocol() //select protocol
        protocolSteps.selectOpenVPN()
        //Add a checked for small packets.
        if(!protocolSteps.smallPacketsToggle.isEnabled) {
            protocolSteps.smallPacketsToggle.click()
        }
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.connectToVPN()
       try {
           val connectionText = mainScreenSteps.connectionText.text.toString()
           waitUntilTextFound(connectionText)
           val vpnIp = mainScreenSteps.IPText.text.toString()
           //val ipRegEx = "^((\\d{1,3})\\.){3}(\\d{1,3})\$"
           val ipRegEx = "^((\\d{1,3})\\.){3}(\\d{1,3})\$"
           // I don't think we are actually checking the ipaddress existence in the UI because
           // there was a short time showing "--" yet it passes the test.
           assertTrue(vpnIp.matches(ipRegEx.toRegex()))
       } catch (e: Exception) {
           fail("Test failed due to an unexpected exception: ${e.message}")
       }
    }

    @Ignore("Ignore this test first while we polish the first test")
    @Test
    fun openVPN_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage() //select settings
        protocolSteps.selectProtocol() //select protocol
        protocolSteps.selectOpenVPN()
        protocolSteps.smallPacketsToggle.click()
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.connectToVPN()
        //Add waitUntilFound()
        //TODo: Assert
    }

    @Ignore("Ignore this test first while we polish the first test")
    @Test
    fun wireGuard_connectivity_when_small_packet_disabled() {
        settingsSteps.navigateToSettingsPage() //select settings
        protocolSteps.selectProtocol() //select protocol
        protocolSteps.selectWireGuard()
        //TODO: Add a checker for use small packets. Checked that it is disabled
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.connectToVPN()
        //Add waitUntilFound()
        //TODo: Assert
    }

    @Ignore("Ignore this test first while we polish the first test")
    @Test
    fun wireGuard_connectivity_when_small_packet_enabled() {
        settingsSteps.navigateToSettingsPage() //select settings
        protocolSteps.selectProtocol() //select protocol
        protocolSteps.selectWireGuard()
        //TODO: Add a checker for use small packets. Checked that it is disabled
        mainScreenSteps.navigateToMainScreen()
        mainScreenSteps.connectToVPN()
        //Add waitUntilFound()
        //TODo: Assert
    }
}
