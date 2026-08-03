package e2e

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

class OpenVpnEncryptionTests {
    @Test
    fun openVpnEncryptionVariantsConnect() =
        uiAutomator {
            login()
            selectProtocol(Protocols.OPEN_VPN_BUTTON)

            listOf(Protocols.AES_128_GCM_BUTTON, Protocols.AES_256_GCM_BUTTON).forEach { encryption ->
                selectDataEncryption(encryption)
                onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
                assertConnected()
                disconnect()
            }
        }
}