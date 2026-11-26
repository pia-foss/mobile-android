package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

private val LATENCY_TAG = "${Main.LOCATION_EIGHT_ITEM}:latency"

class RegionLatencyRefreshTests {
    @Test
    fun pullToRefreshUpdatesServerLatenciesBasedOnConnectivity() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()

            onElement(timeoutMs = REGION_REFRESH_TIMEOUT) { viewIdResourceName == LATENCY_TAG }

            try {
                device.executeShellCommand("svc wifi disable")
                device.executeShellCommand("svc data disable")

                pullToRefreshRegionList()
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