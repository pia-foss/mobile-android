package tests

import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import screens.helpers.UiAutomatorHelpers.waitUntilVisible
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiLogoutTests : UiTest() {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()

        assertTrue(waitUntilVisible(signUpSteps.loginButton))
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()

        UiAutomatorHelpers.device.pressBack()
        mainScreenSteps.navigateToMainScreen()
        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()

        signUpSteps.navigateToSignUpScreen()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()

        settingsSteps.navigateToSettingsPage()
        assertEquals(VpnProtocols.WireGuard.name, protocolSteps.getSelectedProtocol())
    }
}
