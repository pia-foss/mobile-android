package screens.steps

import androidx.test.uiautomator.UiAutomatorTestScope
import screens.helpers.UiAutomatorHelpers.get
import screens.helpers.UiAutomatorHelpers.inputText
import screens.helpers.UiAutomatorHelpers.waitForElement

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
        onElement { viewIdResourceName == SignUpSteps.SIGNUP_LOGIN_BUTTON }.click()
        waitForStableInActiveWindow()
    }

    fun UiAutomatorTestScope.logIn(username: String, password: String) {
        inputText(get(USERNAME_FIELD), username)
        inputText(get(PASSWORD_FIELD), password)
        // hide keyboard
        pressBack()
        get(LOGIN_BUTTON).click()
    }

    fun UiAutomatorTestScope.allowVpnProfileCreation() {
        waitForStableInActiveWindow()
        if (waitForElement(VPN_PROFILE_OK_BUTTON)) {
            get(VPN_PROFILE_OK_BUTTON).click()
            waitForElement(ANDROID_OK_BUTTON)
            get(ANDROID_OK_BUTTON).click()
        }
    }

    fun UiAutomatorTestScope.allowNotifications() {
        waitForStableInActiveWindow()
        if (waitForElement(APP_ALLOW_NOTIFICATIONS)) {
            get(APP_ALLOW_NOTIFICATIONS).click()
            waitForElement(ANDROID_ALLOW_NOTIFICATIONS)
            get(ANDROID_ALLOW_NOTIFICATIONS).click()
        }
    }

    fun UiAutomatorTestScope.giveAppPermissions() {
        allowVpnProfileCreation()
        allowNotifications()
    }
}
