package e2e

import androidx.test.uiautomator.uiAutomator
import org.junit.Test

private val TRUSTED_LOCATIONS = listOf("Singapore", "Norway")
private val LEGACY_LOCATIONS = listOf("Andorra", "Austria")

class WireGuardLocationTests {
    @Test
    fun wireGuardConnectsToTrustedAndLegacyLocations() =
        uiAutomator {
            login()
            selectProtocol(Protocols.WIRE_GUARD_BUTTON)

            (TRUSTED_LOCATIONS + LEGACY_LOCATIONS).forEach { location ->
                connectToLocation(location)
                assertConnected()
                disconnect()
            }
        }
}