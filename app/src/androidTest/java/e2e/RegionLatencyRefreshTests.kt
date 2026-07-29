package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

// Latencies are only pinged for real while the VPN itself is disconnected (see
// RegionListProvider/VpnRegionSelectionViewModel.isVpnConnectionActive) - while connected the
// screen always shows no latency, regardless of device connectivity. So this test never
// connects to the VPN; it only toggles the device's own WiFi/mobile data.
private val LATENCY_TAG = "${Main.LOCATION_EIGHT_ITEM}:latency"

class RegionLatencyRefreshTests {
    @Test
    fun pullToRefreshUpdatesServerLatenciesBasedOnConnectivity() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()

            // Baseline: with real internet, the initial load resolves real per-region latencies.
            onElement(timeoutMs = REGION_REFRESH_TIMEOUT) { viewIdResourceName == LATENCY_TAG }

            // Restoring connectivity is in `finally` so a failed assertion here can't leave
            // wifi/data disabled for the rest of the suite - Test Orchestrator resets app data
            // between tests, but not real OS-level radio state.
            try {
                device.executeShellCommand("svc wifi disable")
                device.executeShellCommand("svc data disable")

                pullToRefreshRegionList()
                // Every ping times out, so no region renders a latency (see LocationPickerItem -
                // it only shows latency text when the value is below VPN_REGIONS_PING_TIMEOUT).
                assertTrue(
                    device.wait(Until.gone(By.res(LATENCY_TAG)), REGION_REFRESH_TIMEOUT),
                )
            } finally {
                device.executeShellCommand("svc wifi enable")
                device.executeShellCommand("svc data enable")
            }

            pullToRefreshRegionList()
            assertNotNull(
                device.wait(Until.hasObject(By.res(LATENCY_TAG)), REGION_REFRESH_TIMEOUT),
            )
        }
}