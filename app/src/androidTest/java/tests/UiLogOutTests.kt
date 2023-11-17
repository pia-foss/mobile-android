package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.MainScreenPageObjects
import screens.objects.SignUpUiObjects

class UiLogOutTests : UiTest() {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        uiAction.signIn("${BuildConfig.PIA_VALID_USERNAME}", "${BuildConfig.PIA_VALID_PASSWORD}")
        uiAction.giveAppPermissions()
        assert(MainScreenPageObjects.connectButton.exists())
        uiAction.logout()
        assert(SignUpUiObjects.loginButton.exists())
    }
}