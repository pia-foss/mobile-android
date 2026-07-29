package e2e

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

private val TRUSTED_LOCATIONS = listOf("Singapore", "Norway")
private val LEGACY_LOCATIONS = listOf("Andorra", "Austria")

class WireGuardLocationAndEncryptionTests {
    @Test
    fun wireGuardConnectsToTrustedAndLegacyLocationsThenOpenVpnEncryptionVariantsConnect() =
        uiAutomator {
            login()
            selectProtocol(Protocols.WIRE_GUARD_BUTTON)

            (TRUSTED_LOCATIONS + LEGACY_LOCATIONS).forEach { location ->
                connectToLocation(location)
                assertConnected()
                disconnect()
            }

            selectProtocol(Protocols.OPEN_VPN_BUTTON)

            listOf(Protocols.AES_128_GCM_BUTTON, Protocols.AES_256_GCM_BUTTON).forEach { encryption ->
                selectDataEncryption(encryption)
                onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
                assertConnected()
                disconnect()
            }
        }
}