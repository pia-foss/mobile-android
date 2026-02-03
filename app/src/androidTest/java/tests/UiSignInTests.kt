package tests

import com.kape.vpn.BuildConfig
import org.junit.Before
import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import kotlin.test.assertNotNull

class UiSignInTests : UiTest() {

    @Before
    override fun setUp() {
        setupWithoutLogin()  // Launch app without auto-login
    }

    @Test
    fun sign_in_with_valid_credentials_reaches_connect_screen() {
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()

        assertNotNull(UiAutomatorHelpers.findByResId(mainScreenSteps.connectButton))
    }

    @Test
    fun sign_in_with_invalid_credentials_does_not_login() {
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("InvalidUser", "InvalidPassword")

        assertNotNull(UiAutomatorHelpers.findByResId(loginSteps.errorField))
    }

    @Test
    fun sign_in_empty_user_and_password_does_not_login() {
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", "")

        assertNotNull(UiAutomatorHelpers.findByResId(loginSteps.errorField))
    }

    @Test
    fun sign_in_valid_user_empty_password_does_not_login() {
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, "")

        assertNotNull(UiAutomatorHelpers.findByResId(loginSteps.errorField))
    }

    @Test
    fun sign_in_empty_user_valid_password_does_not_login() {
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn("", BuildConfig.PIA_VALID_PASSWORD)

        assertNotNull(UiAutomatorHelpers.findByResId(loginSteps.errorField))
    }
}
