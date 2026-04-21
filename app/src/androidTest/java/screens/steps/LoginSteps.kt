package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelper.LONG_TIMEOUT
import screens.helpers.UiAutomatorHelper.click
import screens.helpers.UiAutomatorHelper.findByResId
import screens.helpers.UiAutomatorHelper.hideKeyboard
import screens.helpers.UiAutomatorHelper.inputText

object LoginSteps {
    const val USERNAME_FIELD = ":LoginScreen:enter_username"
    const val PASSWORD_FIELD = ":LoginScreen:enter_password"
    const val ERROR_FIELD = ":LoginScreen:login_error"
    const val LOGIN_BUTTON = ":LoginScreen:login_button"
    const val VPN_PROFILE_OK_BUTTON = ":VpnPermissionScreen:ok"
    const val ANDROID_OK_BUTTON = "android:id/button1"
    const val APP_ALLOW_NOTIFICATIONS = ":NotificationPermissionScreen:notifications_action"
    const val ANDROID_ALLOW_NOTIFICATIONS =
        "com.android.permissioncontroller:id/permission_allow_button"

    fun UiAutomatorTestScope.navigateToLoginScreen() {
        click(SignUpSteps.LOGIN_BUTTON)
    }

    fun UiAutomatorTestScope.logIn(
        username: String,
        password: String,
    ) {
        inputText(USERNAME_FIELD, username)
        inputText(PASSWORD_FIELD, password)
        hideKeyboard()
        click(LOGIN_BUTTON)
    }

    fun UiAutomatorTestScope.allowVpnProfileCreation() {
        findByResId(VPN_PROFILE_OK_BUTTON, LONG_TIMEOUT).takeIf { it != null }?.let {
            findByResId(VPN_PROFILE_OK_BUTTON)?.click()
            findByResId(ANDROID_OK_BUTTON)?.click()
        }
    }

    fun UiAutomatorTestScope.allowNotifications() {
        findByResId(APP_ALLOW_NOTIFICATIONS, LONG_TIMEOUT).takeIf { it != null }?.let {
            findByResId(APP_ALLOW_NOTIFICATIONS)?.click()
            findByResId(ANDROID_ALLOW_NOTIFICATIONS)?.click()
        }
    }

    fun UiAutomatorTestScope.giveAppPermissions() {
        allowVpnProfileCreation()
        allowNotifications()
    }
}