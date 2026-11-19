package e2e

import android.graphics.Rect
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.scrollToElement
import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import java.util.regex.Pattern

object TestCredentials {
    val username: String =
        InstrumentationRegistry.getArguments().getString("PIA_VALID_USERNAME").orEmpty()
    val password: String =
        InstrumentationRegistry.getArguments().getString("PIA_VALID_PASSWORD").orEmpty()
    val dipToken: String =
        InstrumentationRegistry.getArguments().getString("PIA_VALID_DIP_TOKEN").orEmpty()
}

const val INVALID = "invalid"
const val PROTECTED = "Protected"
const val TIMEOUT = 5000L
const val LONG_TIMEOUT = 10000L
private const val DRAWER_SETTLE_POLL_MS = 200L

const val NETWORK_TRANSITION_TIMEOUT = 20000L

const val REGION_REFRESH_TIMEOUT = 30000L

const val SETTINGS_PROPAGATION_TIMEOUT = 15000L

const val TRAFFIC_TIMEOUT = 20000L
val IP_PATTERN =
    Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$",
    )

object RegionSelection {
    const val SEARCH_BAR = ":VpnRegionSelectionScreen:searchBar"
    const val SEARCH_RESULT_ITEM = ":VpnRegionSelectionScreen:locationItem_0"
    const val SEARCH_LOCATION_TEXT = "$SEARCH_RESULT_ITEM:regionName"
    const val SEARCH_RESULT_FAVORITE_ICON = "$SEARCH_RESULT_ITEM:favorite"
    const val REGION = "Moldova"

    const val GEO_REGION = "Bosnia and Herzegovina"
    const val LIST = ":VpnRegionSelectionScreen:list"
    const val FAVORITES_HEADING = ":VpnRegionSelectionScreen:favoritesHeading"

    const val FIRST_FAVORITE_NAME = ":VpnRegionSelectionScreen:locationItem_1:regionName"
}

object Dip {
    const val FIELD = ":DedicatedIPScreen:dip_text_field"
    const val ACTIVATE_BUTTON = ":DedicatedIPScreen:activate_button"
    const val FLAG = ":DedicatedIPScreen:dip_flag"
    const val SERVER_NAME = ":DedicatedIPScreen:dip_server_name"
}

object Login {
    const val USERNAME_FIELD = ":LoginScreen:enter_username"
    const val PASSWORD_FIELD = ":LoginScreen:enter_password"
    const val ERROR_FIELD = ":LoginScreen:login_error"
    const val LOGIN_BUTTON = ":LoginScreen:login_button"
    const val VPN_PROFILE_OK_BUTTON = ":VpnPermissionScreen:ok"
    const val ANDROID_OK_BUTTON = "android:id/button1"
    const val APP_ALLOW_NOTIFICATIONS = ":NotificationPermissionScreen:notifications_action"
    const val ANDROID_ALLOW_NOTIFICATIONS =
        "com.android.permissioncontroller:id/permission_allow_button"
}

object Main {
    const val CONNECT_BUTTON = ":ConnectionScreen:connection_button"
    const val SIDE_MENU = ":AppBar:side_menu"
    const val REORDER_BUTTON = ":AppBar:reorder"
    const val QUICK_CONNECT_FIRST_ITEM = ":QuickConnect:server_0"
    const val QUICK_CONNECT_SECOND_ITEM = ":QuickConnect:server_1"
    const val LOCATION_PICKER = ":ConnectionScreen:VpnLocationPicker"
    const val LOCATION_EIGHT_ITEM = ":VpnRegionSelectionScreen:locationItem_8"
    const val VPN_IP = ":Text:vpnIp"
    const val CHANGE_LOCATION_CONFIRM = ":ChangeLocation:confirm"
}

object Customization {
    const val ELEMENTS_LIST = ":CustomizationScreen:elements_list"
    const val TRAFFIC_VISIBILITY_TOGGLE = ":CustomizationScreen:visibility_traffic"

    // "conection-info" (sic) matches the misspelled name baked into
    // CustomizationPrefs.defaultList() - do not "fix" the typo here, it must match exactly.
    const val CONNECTION_INFO_VISIBILITY_TOGGLE = ":CustomizationScreen:visibility_conection-info"
    const val SAVE_BUTTON = ":AppBar:save"
}

object Traffic {
    const val DOWNLOAD = ":Text:trafficDownload"
    const val UPLOAD = ":Text:trafficUpload"
    const val ZERO_BYTES = "0 B"
}

object QuickSettings {
    const val AUTOMATION_BUTTON = ":QuickSettings:automation"
    const val KILL_SWITCH_BUTTON = ":QuickSettings:kill_switch"
    const val PROTOCOLS_BUTTON = ":QuickSettings:protocols"
}

object ConnectionInfoTile {
    const val CONNECTION = ":Text:connectionInfoConnection"
    const val PORT = ":Text:connectionInfoPort"
    const val AUTH = ":Text:connectionInfoAuth"
    const val TRANSPORT = ":Text:connectionInfoTransport"
    const val ENCRYPTION = ":Text:connectionInfoEncryption"
    const val HANDSHAKE = ":Text:connectionInfoHandshake"
}

object Protocols {
    const val PROTOCOL_SELECTION_BUTTON = ":ProtocolSettingsScreen:protocol_selection"
    const val OPEN_VPN_BUTTON = ":OptionsDialog:OpenVPN"
    const val WIRE_GUARD_BUTTON = ":OptionsDialog:WireGuard"
    const val ANDROID_OK_BUTTON = ":OptionsDialog:Ok"
    const val SMALL_PACKETS_TOGGLE = ":ProtocolSettingsScreen:use_small_packets"
    const val TRANSPORT_BUTTON = ":ProtocolSettingsScreen:transport"
    const val UDP_BUTTON = ":OptionsDialog:UDP"
    const val TCP_BUTTON = ":OptionsDialog:TCP"
    const val DATA_ENCRYPTION_BUTTON = ":ProtocolSettingsScreen:data_encryption"
    const val AES_128_GCM_BUTTON = ":OptionsDialog:AES-128-GCM"
    const val AES_256_GCM_BUTTON = ":OptionsDialog:AES-256-GCM"
}

object Settings {
    const val GENERAL_BUTTON = ":SettingsScreen:General"
    const val PROTOCOLS_BUTTON = ":SettingsScreen:Protocols"
    const val AUTOMATION_BUTTON = ":SettingsScreen:Automation"
    const val HELP_BUTTON = ":SettingsScreen:Help"
}

object Help {
    const val VERSION = ":HelpScreen:Version"
    const val VIEW_DEBUG_LOG = ":HelpScreen:ViewDebugLog"
    const val IMPROVE_PIA_TOGGLE = ":HelpScreen:ImprovePia"
    const val VIEW_SHARED_DATA = ":HelpScreen:ViewSharedData"
}

object GeneralSettings {
    const val GEO_SERVERS_TOGGLE = ":GeneralSettingsScreen:geo_servers_toggle"
}

object Automation {
    const val ENABLE_TOGGLE = ":AutomationSettingsScreen:enable_toggle"
    const val MANAGE_AUTOMATION = ":AutomationSettingsScreen:manage_automation"
    const val WIFI_RULE_CARD = ":AutomationScreen:networkCard_WifiOpen"
    const val MOBILE_DATA_RULE_CARD = ":AutomationScreen:networkCard_MobileData"
}

object AutoProtocolNudge {
    const val DISMISS_BUTTON = ":SideMenu:DismissButton"
}

object BehaviorDialog {
    const val CONNECT_OPTION = ":BehaviorDialog:option_0"
    const val DISCONNECT_OPTION = ":BehaviorDialog:option_1"
    const val OK_BUTTON = ":BehaviorDialog:ok"
}

object SignUp {
    const val LOGIN_BUTTON = ":SignUpScreen:Login"
}

object Consent {
    const val NO_THANKS_BUTTON = ":ConsentScreen:NoThanks"
}

object SideMenu {
    const val ACCOUNT = ":SideMenu:Account"
    const val SETTINGS_BUTTON = ":SideMenu:Settings"
    const val DEDICATED_IP = ":SideMenu:DedicatedIP"
    const val PER_APP_SETTINGS = ":SideMenu:PerAppSettings"
    const val LOGOUT_BUTTON = ":SideMenu:Logout"
    const val LOGOUT_DIALOG_CONFIRM_BUTTON = ":SideMenu:ConfirmButton"
    const val LOGOUT_DIALOG_DISMISS_BUTTON = ":SideMenu:DismissButton"
    const val ABOUT = ":SideMenu:About"
    const val PRIVACY_POLICY = ":SideMenu:PrivacyPolicy"
    const val CONTACT_SUPPORT = ":SideMenu:ContactSupport"
    const val USERNAME = ":SideMenu:Username"
}

fun reachLogin() =
    uiAutomator {
        device.executeShellCommand("appops set ${BuildConfig.APPLICATION_ID} ACTIVATE_VPN allow")
        startApp(BuildConfig.APPLICATION_ID)
        onElement(timeoutMs = LONG_TIMEOUT) { viewIdResourceName == Consent.NO_THANKS_BUTTON }.click()
        onElement(timeoutMs = LONG_TIMEOUT) { viewIdResourceName == SignUp.LOGIN_BUTTON }.click()
    }

fun login() =
    uiAutomator {
        reachLogin()
        onElement { viewIdResourceName == Login.USERNAME_FIELD }.text =
            TestCredentials.username
        onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text =
            TestCredentials.password
        onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.VPN_PROFILE_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.APP_ALLOW_NOTIFICATIONS }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_ALLOW_NOTIFICATIONS }?.click()
    }

fun loginFromCurrent() =
    uiAutomator {
        onElement { viewIdResourceName == SignUp.LOGIN_BUTTON }.click()
        onElement { viewIdResourceName == Login.USERNAME_FIELD }.text =
            TestCredentials.username
        onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text =
            TestCredentials.password
        onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.VPN_PROFILE_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.APP_ALLOW_NOTIFICATIONS }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_ALLOW_NOTIFICATIONS }?.click()
    }

fun assertConnected(timeoutMs: Long = LONG_TIMEOUT) =
    uiAutomator {
        assertNotNull(
            device.wait(Until.hasObject(By.textStartsWith(PROTECTED)), timeoutMs),
        )
        assertNotNull(
            device.wait(
                Until.hasObject(By.res(Main.VPN_IP).text(IP_PATTERN)),
                timeoutMs,
            ),
        )
    }

fun assertDisconnected(timeoutMs: Long = LONG_TIMEOUT) =
    uiAutomator {
        assertTrue(
            device.wait(Until.gone(By.res(Main.VPN_IP).text(IP_PATTERN)), timeoutMs),
        )
    }

private fun openProtocolsSettings() =
    uiAutomator {
        openSideMenu()
        onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
        onElement { viewIdResourceName == Settings.PROTOCOLS_BUTTON }.click()
    }

private fun closeSettings() =
    uiAutomator {
        device.pressBack()
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }

fun selectProtocol(protocolButton: String) =
    uiAutomator {
        openProtocolsSettings()
        onElement { viewIdResourceName == Protocols.PROTOCOL_SELECTION_BUTTON }.click()
        onElement { viewIdResourceName == protocolButton }.click()
        onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
        closeSettings()
    }

fun selectDataEncryption(encryptionButton: String) =
    uiAutomator {
        openProtocolsSettings()
        onElement { viewIdResourceName == Protocols.DATA_ENCRYPTION_BUTTON }.click()
        onElement { viewIdResourceName == encryptionButton }.click()
        onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
        closeSettings()
    }

fun selectTransport(transportButton: String) =
    uiAutomator {
        openProtocolsSettings()
        onElement { viewIdResourceName == Protocols.TRANSPORT_BUTTON }.click()
        onElement { viewIdResourceName == transportButton }.click()
        onElement { viewIdResourceName == Protocols.ANDROID_OK_BUTTON }.click()
        closeSettings()
    }

fun toggleGeoLocatedServers() =
    uiAutomator {
        openSideMenu()
        onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
        onElement { viewIdResourceName == Settings.GENERAL_BUTTON }.click()
        onElement { viewIdResourceName == GeneralSettings.GEO_SERVERS_TOGGLE }.click()
        returnToConnectionScreen()
    }

fun searchRegion(location: String) =
    uiAutomator {
        onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.click()
        onElement { viewIdResourceName == RegionSelection.SEARCH_BAR }.text = location
    }

fun connectToLocation(location: String) =
    uiAutomator {
        onElement { viewIdResourceName == Main.LOCATION_PICKER }.click()
        searchRegion(location)
        onElement { viewIdResourceName == RegionSelection.SEARCH_LOCATION_TEXT }.click()
    }

fun pullToRefreshRegionList() =
    uiAutomator {
        onElement { viewIdResourceName == RegionSelection.LIST }.swipe(Direction.DOWN, 1f)
    }

fun dismissAutoProtocolNudgeIfShown() =
    uiAutomator {
        onElementOrNull(TIMEOUT) { viewIdResourceName == AutoProtocolNudge.DISMISS_BUTTON }?.click()
    }

fun disconnect() =
    uiAutomator {
        onElement { viewIdResourceName == Main.CONNECT_BUTTON }.click()
        device.waitForIdle()
    }

private fun enableCustomizationElement(visibilityToggle: String) =
    uiAutomator {
        onElement { viewIdResourceName == Main.REORDER_BUTTON }.click()
        onElement { viewIdResourceName == Customization.ELEMENTS_LIST }
            .scrollToElement(Direction.DOWN, timeoutMs = LONG_TIMEOUT) {
                viewIdResourceName == visibilityToggle
            }.click()
        onElement { viewIdResourceName == Customization.SAVE_BUTTON }.click()
        device.waitForIdle()
    }

// Taps on menu items are ignored until the drawer has finished sliding in, and the items exist
// well before that, so wait until the drawer stops moving.
fun openSideMenu() =
    uiAutomator {
        device.waitForIdle()
        onElement(timeoutMs = NETWORK_TRANSITION_TIMEOUT) { viewIdResourceName == Main.SIDE_MENU }.click()
        val deadline = System.currentTimeMillis() + TIMEOUT
        var previous: Rect? = null
        var current = device.findObject(By.res(SideMenu.USERNAME))?.visibleBounds
        while ((current == null || current != previous) && System.currentTimeMillis() < deadline) {
            Thread.sleep(DRAWER_SETTLE_POLL_MS)
            previous = current
            current = device.findObject(By.res(SideMenu.USERNAME))?.visibleBounds
        }
        assertTrue("Side menu did not finish opening", current != null && current == previous)
    }

fun enableTrafficTile() =
    uiAutomator {
        enableCustomizationElement(Customization.TRAFFIC_VISIBILITY_TOGGLE)
    }

fun enableConnectionInfoTile() =
    uiAutomator {
        enableCustomizationElement(Customization.CONNECTION_INFO_VISIBILITY_TOGGLE)
    }

// The screen only becomes scrollable once its tiles don't fit, so it may have nothing to scroll.
private fun scrollConnectionScreenToElement(targetViewId: String) =
    uiAutomator {
        if (!device.hasObject(By.res(targetViewId))) {
            onElement { isScrollable }.scrollToElement(Direction.DOWN, timeoutMs = LONG_TIMEOUT) {
                viewIdResourceName == targetViewId
            }
        }
    }

fun scrollToConnectionInfoTile() =
    uiAutomator {
        scrollConnectionScreenToElement(ConnectionInfoTile.CONNECTION)
    }

fun scrollToTrafficTile() =
    uiAutomator {
        scrollConnectionScreenToElement(Traffic.DOWNLOAD)
    }

fun enableAutomation() =
    uiAutomator {
        device.executeShellCommand(
            "pm grant ${BuildConfig.APPLICATION_ID} android.permission.ACCESS_FINE_LOCATION",
        )
        device.executeShellCommand(
            "pm grant ${BuildConfig.APPLICATION_ID} android.permission.ACCESS_BACKGROUND_LOCATION",
        )

        openSideMenu()
        onElement { viewIdResourceName == SideMenu.SETTINGS_BUTTON }.click()
        onElement { viewIdResourceName == Settings.AUTOMATION_BUTTON }.click()
        onElement { viewIdResourceName == Automation.ENABLE_TOGGLE }.click()

        onElementOrNull(TIMEOUT) { viewIdResourceName == Automation.MANAGE_AUTOMATION }?.click()
    }

fun setNetworkRuleBehavior(
    networkCardTag: String,
    behaviorOptionTag: String,
) = uiAutomator {
    onElement(timeoutMs = LONG_TIMEOUT) { viewIdResourceName == networkCardTag }.click()
    onElement { viewIdResourceName == behaviorOptionTag }.click()
    onElement { viewIdResourceName == BehaviorDialog.OK_BUTTON }.click()
}

fun assertInteractableAndClick(viewId: String) =
    uiAutomator {
        val item = onElement { viewIdResourceName == viewId }
        assertTrue(item.isEnabled)
        item.click()
    }

fun navigateToHelpScreen() =
    uiAutomator {
        openSideMenu()
        assertInteractableAndClick(SideMenu.SETTINGS_BUTTON)
        assertInteractableAndClick(Settings.HELP_BUTTON)
        assertNotNull(
            device.wait(Until.hasObject(By.textContains("Help")), TIMEOUT),
        )
    }

fun returnToConnectionScreen() =
    uiAutomator {
        var attempts = 0
        while (onElementOrNull(500L) { viewIdResourceName == Main.CONNECT_BUTTON } == null && attempts < 10) {
            device.pressBack()
            device.waitForIdle()
            attempts++
        }
    }