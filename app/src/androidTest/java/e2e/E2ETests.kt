package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class E2ETests {
    // Connection tests
    @Test
    fun connectAfterLogin() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()
        }

    @Test
    fun searchAndConnectToRegion() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.click()
            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.text = RegionSelection.REGION
            onElement { viewIdResourceName == RegionSelection.SEARCH_LOCATION_TEXT }.click()
            assertConnected()
        }

    @Test
    fun automationConnectsAndDisconnectsVpnOnWifiToggle() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()

            enableAutomation()
            setNetworkRuleBehavior(Automation.WIFI_RULE_CARD, BehaviorDialog.CONNECT_OPTION)
            setNetworkRuleBehavior(Automation.MOBILE_DATA_RULE_CARD, BehaviorDialog.DISCONNECT_OPTION)
            returnToConnectionScreen()

            // Restoring wifi is in `finally` so a failed assertion here can't leave it disabled
            // for the rest of the suite - Test Orchestrator resets app data between tests, but
            // not real OS-level radio state.
            try {
                device.executeShellCommand("svc wifi disable")
                assertDisconnected(NETWORK_TRANSITION_TIMEOUT)
            } finally {
                device.executeShellCommand("svc wifi enable")
            }

            assertConnected(NETWORK_TRANSITION_TIMEOUT)
        }

    @Test
    fun connectionInfoUpdatesWhenProtocolSettingsChange() =
        uiAutomator {
            login()
            enableConnectionInfoTile()
            selectProtocol(Protocols.OPEN_VPN_BUTTON)

            scrollToConnectionInfoTile()
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == ConnectionInfoTile.CONNECTION && textAsString() == "OpenVPN"
            }
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == ConnectionInfoTile.TRANSPORT && textAsString() == "UDP"
            }
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == ConnectionInfoTile.ENCRYPTION && textAsString() == "AES-128-GCM"
            }

            selectTransport(Protocols.TCP_BUTTON)
            scrollToConnectionInfoTile()
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == ConnectionInfoTile.TRANSPORT && textAsString() == "TCP"
            }

            selectDataEncryption(Protocols.AES_256_GCM_BUTTON)
            scrollToConnectionInfoTile()
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == ConnectionInfoTile.ENCRYPTION && textAsString() == "AES-256-GCM"
            }
        }

    @Test
    fun markingAndUnmarkingRegionAsFavoriteUpdatesFavoritesSection() =
        uiAutomator {
            login()
            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            searchRegion(RegionSelection.REGION)

            // Favoriting is a DataStore write + StateFlow re-collection round trip (see
            // SETTINGS_PROPAGATION_TIMEOUT) before the search results list re-renders with the
            // matching row, which can take longer than a plain UI wait on a constrained CI emulator.
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == RegionSelection.SEARCH_RESULT_FAVORITE_ICON
            }.click()

            // Clearing the search text drops back to the full, unfiltered list, where a
            // favorited region gets pinned under its own heading.
            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.text = ""
            assertNotNull(
                device.wait(Until.hasObject(By.res(RegionSelection.FAVORITES_HEADING)), TIMEOUT),
            )
            assertNotNull(
                device.wait(
                    Until.hasObject(By.res(RegionSelection.FIRST_FAVORITE_NAME).text(RegionSelection.REGION)),
                    TIMEOUT,
                ),
            )

            searchRegion(RegionSelection.REGION)
            onElement(timeoutMs = SETTINGS_PROPAGATION_TIMEOUT) {
                viewIdResourceName == RegionSelection.SEARCH_RESULT_FAVORITE_ICON
            }.click()

            onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.text = ""
            assertTrue(
                device.wait(Until.gone(By.res(RegionSelection.FAVORITES_HEADING)), TIMEOUT),
            )
        }

    @Test
    fun disablingGeoLocatedServersHidesThemFromTheRegionList() =
        uiAutomator {
            login()

            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            searchRegion(RegionSelection.GEO_REGION)
            assertNotNull(
                device.wait(
                    Until.hasObject(By.res(RegionSelection.SEARCH_LOCATION_TEXT).text(RegionSelection.GEO_REGION)),
                    TIMEOUT,
                ),
            )
            returnToConnectionScreen()

            toggleGeoLocatedServers()

            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            searchRegion(RegionSelection.GEO_REGION)
            assertTrue(
                device.wait(Until.gone(By.res(RegionSelection.SEARCH_LOCATION_TEXT)), TIMEOUT),
            )
            returnToConnectionScreen()

            // Restore the default so other tests aren't affected by this one.
            toggleGeoLocatedServers()

            onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
            searchRegion(RegionSelection.GEO_REGION)
            assertNotNull(
                device.wait(
                    Until.hasObject(By.res(RegionSelection.SEARCH_LOCATION_TEXT).text(RegionSelection.GEO_REGION)),
                    TIMEOUT,
                ),
            )
        }

    @Test
    fun downloadAndUploadArePopulatedOnceConnected() =
        uiAutomator {
            login()
            enableTrafficTile()

            onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
            assertConnected()

            scrollToTrafficTile()
            onElement(timeoutMs = TRAFFIC_TIMEOUT) {
                viewIdResourceName == Traffic.DOWNLOAD && textAsString() != Traffic.ZERO_BYTES
            }
            onElement(timeoutMs = TRAFFIC_TIMEOUT) {
                viewIdResourceName == Traffic.UPLOAD && textAsString() != Traffic.ZERO_BYTES
            }
        }
}