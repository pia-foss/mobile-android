package tests

import com.kape.vpn.BuildConfig
import org.junit.Before
import org.junit.Test
import screens.helpers.UiAutomatorHelpers.waitUntilVisible
import kotlin.test.assertTrue

class UiSignInTests : UiTest() {

    @Before
    override fun setUp() {
        setupWithoutLogin()  // Launch app without auto-login
    }

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()

        assertTrue(waitUntilVisible(mainScreenSteps.connectButton))
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("InvalidUser", "InvalidPassword")

        assertTrue(waitUntilVisible(loginSteps.errorField))
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", "")

        assertTrue(waitUntilVisible(loginSteps.errorField))
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, "")

        assertTrue(waitUntilVisible(loginSteps.errorField))
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", BuildConfig.PIA_VALID_PASSWORD)

        assertTrue(waitUntilVisible(loginSteps.errorField))
    }
}
