package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import screens.helpers.UiAutomatorHelpers.LONG_TIMEOUT
import screens.helpers.UiAutomatorHelpers.click
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.MainScreenSteps
import screens.steps.MainScreenSteps.CONNECT_BUTTON
import screens.steps.SignUpSteps
import screens.steps.SideMenuSteps
open class UiTest() {

    fun setupWithLogin() = uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
        // If already logged in (main screen), no login needed
        val loginButton = findByResId(SignUpSteps.LOGIN_BUTTON)
        if (loginButton != null) {
            loginButton.click()
            logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
            giveAppPermissions()
        }
    }

    /**
     * Like setupWithLogin, but always logs out first to wipe persistence state.
     * Use this when tests depend on a clean persistence layer (e.g. DIP, protocol settings).
     */
    fun setupWithFreshLogin() = uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
        // If logged in, log out first to clear persistent state
        if (findByResId(MainScreenSteps.SIDE_MENU) != null) {
            click(MainScreenSteps.SIDE_MENU)
            click(SideMenuSteps.LOGOUT_BUTTON)
            click(SideMenuSteps.LOGOUT_DIALOG_CONFIRM_BUTTON)
            // Confirm we reached the SignUp screen before proceeding
            findByResId(SignUpSteps.LOGIN_BUTTON, LONG_TIMEOUT)
                ?: error("SignUp screen did not appear after logout")
        }
        // On SignUp screen — log in fresh
        click(SignUpSteps.LOGIN_BUTTON)
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()
        // Wait for the connection screen to be fully ready before returning.
        // This ensures all post-login initialization (VPN service, server loading)
        // has settled, so callers can immediately interact with the main screen.
        findByResId(CONNECT_BUTTON, LONG_TIMEOUT)
            ?: error("Connection screen did not appear after login")
    }

    fun setupWithoutLogin() = uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
    }
}
