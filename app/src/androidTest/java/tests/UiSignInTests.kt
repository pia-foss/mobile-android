package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.elementExists
import screens.helpers.UiAutomatorHelpers.launchApp
import screens.helpers.UiAutomatorHelpers.waitForElement
import screens.steps.LoginSteps
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.LoginSteps.navigateToLoginScreen
import screens.steps.MainScreenSteps
import kotlin.test.assertTrue

class UiSignInTests {

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() = uiAutomator {
        launchApp()
        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()

        waitForElement(MainScreenSteps.CONNECT_BUTTON)
        assertTrue { elementExists(MainScreenSteps.CONNECT_BUTTON) }
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() = uiAutomator {
        launchApp()
        navigateToLoginScreen()
        logIn("InvalidUser", "InvalidPassword")
        waitForElement(LoginSteps.ERROR_FIELD)

        assertTrue(elementExists(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() = uiAutomator {
        launchApp()
        navigateToLoginScreen()
        logIn("", "")

        assertTrue(elementExists(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() = uiAutomator {
        launchApp()
        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, "")

        assertTrue(elementExists(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() = uiAutomator {
        launchApp()
        navigateToLoginScreen()
        logIn("", BuildConfig.PIA_VALID_PASSWORD)

        assertTrue(elementExists(LoginSteps.ERROR_FIELD))
    }

}
