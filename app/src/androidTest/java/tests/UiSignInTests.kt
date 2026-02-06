package tests

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.BuildConfig
import org.junit.Before
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.findByResId
import screens.steps.LoginSteps
import screens.steps.LoginSteps.giveAppPermissions
import screens.steps.LoginSteps.logIn
import screens.steps.LoginSteps.navigateToLoginScreen
import screens.steps.MainScreenSteps
import kotlin.test.assertNotNull

class UiSignInTests : UiTest() {

    @Before
    override fun setUp() {
        setupWithoutLogin()  // Launch app without auto-login
    }

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() = uiAutomator {
        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        giveAppPermissions()

        assertNotNull(findByResId(MainScreenSteps.CONNECT_BUTTON))
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() = uiAutomator {
        navigateToLoginScreen()
        logIn("InvalidUser", "InvalidPassword")

        assertNotNull(findByResId(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() = uiAutomator {
        navigateToLoginScreen()
        logIn("", "")

        assertNotNull(findByResId(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() = uiAutomator {
        navigateToLoginScreen()
        logIn(BuildConfig.PIA_VALID_USERNAME, "")

        assertNotNull(findByResId(LoginSteps.ERROR_FIELD))
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() = uiAutomator {
        navigateToLoginScreen()
        logIn("", BuildConfig.PIA_VALID_PASSWORD)

        assertNotNull(findByResId(LoginSteps.ERROR_FIELD))
    }
}
