package e2e

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import junit.framework.TestCase.assertNotNull
import java.util.regex.Pattern

const val INVALID = "invalid"
const val PROTECTED = "Protected"
const val TIMEOUT = 5000L
const val LONG_TIMEOUT = 10000L
val IP_PATTERN =
    Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$",
    )

object RegionSelection {
    const val SEARCH_BAR = ":VpnRegionSelectionScreen:searchBar"
    const val SEARCH_LOCATION_TEXT = ":VpnRegionSelectionScreen:locationItem_0:regionName"
    const val REGION = "Moldova"
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
    const val QUICK_CONNECT_FIRST_ITEM = ":QuickConnect:server_0"
    const val QUICK_CONNECT_SECOND_ITEM = ":QuickConnect:server_1"
    const val LOCATION_PICKER = ":ConnectionScreen:VpnLocationPicker"
    const val LOCATION_EIGHT_ITEM = ":VpnRegionSelectionScreen:locationItem_8"
    const val VPN_IP = ":Text:vpnIp"
    const val CHANGE_LOCATION_CONFIRM = ":ChangeLocation:confirm"
}

object Protocols {
    const val PROTOCOL_SELECTION_BUTTON = ":ProtocolSettingsScreen:protocol_selection"
    const val OPEN_VPN_BUTTON = ":OptionsDialog:OpenVPN"
    const val WIRE_GUARD_BUTTON = ":OptionsDialog:WireGuard"
    const val ANDROID_OK_BUTTON = ":OptionsDialog:Ok"
    const val SMALL_PACKETS_TOGGLE = ":ProtocolSettingsScreen:use_small_packets"
}

object Settings {
    const val PROTOCOLS_BUTTON = ":SettingsScreen:Protocols"
}

object SignUp {
    const val LOGIN_BUTTON = ":SignUpScreen:Login"
}

object SideMenu {
    const val SETTINGS_BUTTON = ":SideMenu:Settings"
    const val DEDICATED_IP = ":SideMenu:DedicatedIP"
    const val LOGOUT_BUTTON = ":SideMenu:Logout"
    const val LOGOUT_DIALOG_CONFIRM_BUTTON = ":SideMenu:ConfirmButton"
    const val USERNAME = ":SideMenu:Username"
}

fun reachLogin() =
    uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        onElement(timeoutMs = LONG_TIMEOUT) { viewIdResourceName == SignUp.LOGIN_BUTTON }.click()
    }

fun login() =
    uiAutomator {
        reachLogin()
        onElement { viewIdResourceName == Login.USERNAME_FIELD }.text =
            BuildConfig.PIA_VALID_USERNAME
        onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text =
            BuildConfig.PIA_VALID_PASSWORD
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
            BuildConfig.PIA_VALID_USERNAME
        onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text =
            BuildConfig.PIA_VALID_PASSWORD
        onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.VPN_PROFILE_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_OK_BUTTON }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.APP_ALLOW_NOTIFICATIONS }?.click()
        onElementOrNull(TIMEOUT) { viewIdResourceName == Login.ANDROID_ALLOW_NOTIFICATIONS }?.click()
    }

fun assertConnected() =
    uiAutomator {
        assertNotNull(
            device.wait(Until.hasObject(By.textStartsWith(PROTECTED)), TIMEOUT),
        )
        assertNotNull(
            device.wait(
                Until.hasObject(By.res(Main.VPN_IP).text(IP_PATTERN)),
                LONG_TIMEOUT,
            ),
        )
    }