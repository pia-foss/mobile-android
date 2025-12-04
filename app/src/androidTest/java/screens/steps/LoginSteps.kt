package screens.steps

import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible

object LoginSteps {

    private val device get() = UiAutomatorHelpers.device

    val usernameField get() = UiAutomatorHelpers.findByResId(":LoginScreen:enter_username")
    val passwordField get() = UiAutomatorHelpers.findByResId(":LoginScreen:enter_password")
    val errorField get() = UiAutomatorHelpers.findByResId(":LoginScreen:login_error")
    val loginButton get() = UiAutomatorHelpers.findByResId(":LoginScreen:login_button")
    val vpnProfileOkButton get() = UiAutomatorHelpers.findByResId(":VpnPermissionScreen:ok")
    val androidOkButton get() = UiAutomatorHelpers.findByResId("android:id/button1")
    val appAllowNotifications get() = UiAutomatorHelpers.findByResId(":NotificationPermissionScreen:notifications_action")
    val androidAllowNotifications get() = UiAutomatorHelpers.findByResId("com.android.permissioncontroller:id/permission_allow_button")

    fun navigateToLoginScreen() {
        SignUpSteps.loginButton?.let { UiAutomatorHelpers.click(it) }
        waitUntilVisible(usernameField)
    }

    fun logIn(username: String, password: String) {
        waitUntilVisible(usernameField)
        UiAutomatorHelpers.inputText(usernameField, username)
        UiAutomatorHelpers.inputText(passwordField, password)
        UiAutomatorHelpers.click(loginButton)
    }

    fun allowVpnProfileCreation() {
        vpnProfileOkButton?.takeIf { waitUntilVisible(it) }?.let {
            UiAutomatorHelpers.click(it)
            UiAutomatorHelpers.click(androidOkButton)
        }
    }

    fun allowNotifications() {
        appAllowNotifications?.takeIf { waitUntilVisible(it) }?.let {
            UiAutomatorHelpers.click(it)
            UiAutomatorHelpers.click(androidAllowNotifications)
        }
    }

    fun giveAppPermissions() {
        allowVpnProfileCreation()
        allowNotifications()
    }
}
