package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.DedicatedIPObjects

class UiDedicatedIPTests : UiTest() {

    @Test
    fun accept_valid_dedicated_ip_token() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        dedicateIPObjects.navigateToDedicatedIPPage()
        dedicateIPObjects.activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)
        assert(DedicatedIPObjects.dedicatedIPServerName.exists()
                && DedicatedIPObjects.dedicatedIPFlag.exists()
                && !DedicatedIPObjects.dedicatedIPField.exists())
    }

    @Test
    fun reject_invalid_dedicated_ip_token() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        dedicateIPObjects.navigateToDedicatedIPPage()
        dedicateIPObjects.activateDedicatedIPToken("InvalidToken")
        assert(DedicatedIPObjects.dedicatedIPField.exists())
    }

    @Test
    fun reject_empty_dedicated_ip_token() {
        signUpUiObjects.navigateToSignUpScreen()
        loginUiObjects.navigateToLoginScreen()
        loginUiObjects.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginUiObjects.giveAppPermissions()
        dedicateIPObjects.navigateToDedicatedIPPage()
        dedicateIPObjects.activateDedicatedIPToken("")
        assert(DedicatedIPObjects.dedicatedIPField.exists())
    }
}
