package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.SignUpSteps
import kotlin.test.assertNotNull

open class UiTest() {

    /**
     * Default setup: launches app and logs in with valid credentials.
     */
    @org.junit.Before
    open fun setUp() = uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
        // Wait for SignUp screen
        assertNotNull(findByResId(SignUpSteps.LOGIN_BUTTON))
        // Navigate to login
        findByResId(SignUpSteps.LOGIN_BUTTON)?.click()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()
    }

    /**
     * Setup variant: launches app without logging in.
     * Useful for tests that start from SignUp screen.
     */
    @org.junit.Before
    fun setupWithoutLogin() = uiAutomator {
        startApp(BuildConfig.APPLICATION_ID)
        waitForAppToBeVisible(BuildConfig.APPLICATION_ID)
    }
}
