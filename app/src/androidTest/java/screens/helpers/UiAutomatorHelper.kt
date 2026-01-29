package screens.helpers

import androidx.test.uiautomator.ElementNotFoundException
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import com.kape.vpn.BuildConfig
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.MainScreenSteps
import screens.steps.SignUpSteps

object UiAutomatorHelpers {

    private const val DEFAULT_TIMEOUT = 5000L
    private const val POLL_INTERVAL = 200L

    fun UiAutomatorTestScope.inputText(where: UiObject2, what: String) {
        where.apply {
            click()
            device.executeShellCommand("input text $what")
        }
    }

    fun UiAutomatorTestScope.get(resId: String) = onElement(100) { viewIdResourceName == resId }

    fun UiAutomatorTestScope.launchApp() {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
    }

    fun UiAutomatorTestScope.launchAppAndLogIn() {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)

        waitForElement(SignUpSteps.SIGNUP_LOGIN_BUTTON)
        get(SignUpSteps.SIGNUP_LOGIN_BUTTON).click()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()
        waitForElement(MainScreenSteps.CONNECT_BUTTON)
    }

    fun UiAutomatorTestScope.logIn() {
        waitForElement(SignUpSteps.SIGNUP_LOGIN_BUTTON)
        get(SignUpSteps.SIGNUP_LOGIN_BUTTON).click()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()
        waitForElement(MainScreenSteps.SIDE_MENU)
    }

    fun UiAutomatorTestScope.elementExists(resId: String): Boolean {
        return try {
            onElement(500) { viewIdResourceName == resId }
            true
        } catch (e: ElementNotFoundException) {
            false
        }
    }

    fun UiAutomatorTestScope.waitForElement(
        resId: String,
        timeout: Long = DEFAULT_TIMEOUT,
        pollInterval: Long = POLL_INTERVAL,
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                onElement(pollInterval) { viewIdResourceName == resId }
                return true
            } catch (_: Exception) { /* keep polling */
            }
            Thread.sleep(pollInterval)
        }
        return false
    }

    fun UiAutomatorTestScope.waitUntilConnectionIsEstablished(
        timeout: Long = DEFAULT_TIMEOUT,
        pollInterval: Long = POLL_INTERVAL,
    ): Boolean {
        val vpnIpRegex = "^((\\d{1,3})\\.){3}(\\d{1,3})$".toRegex()
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeout) {
            val vpnIpField = try {
                onElement(pollInterval) { viewIdResourceName == ":Text:vpnIp" }
            } catch (e: Exception) {
                null
            }

            if (vpnIpField != null && vpnIpRegex.matches(vpnIpField.text)) {
                return true
            }

            Thread.sleep(pollInterval)
        }

        return false
    }
}
