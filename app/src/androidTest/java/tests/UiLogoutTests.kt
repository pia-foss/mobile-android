package tests

import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorHelpers
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiLogoutTests : UiTest() {

    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()

        assertNotNull(UiAutomatorHelpers.findByResId(signUpSteps.loginButton))
    }

    @Test
    fun persistence_layer_wiped_after_sign_out() {
        settingsSteps.navigateToSettingsPage()
        protocolSteps.selectProtocol()
        protocolSteps.selectOpenVPN()

        UiAutomatorHelpers.pressBackTwice(2)

        sideMenuSteps.navigateToSideMenu()
        sideMenuSteps.logOut()

        UiAutomatorHelpers.device.waitForIdle()
        loginSteps.navigateToLoginScreen()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)

        settingsSteps.navigateToSettingsPage()
        assertNotNull(UiAutomatorHelpers.findByResId(settingsSteps.protocolsButton))
        assertNotNull(UiAutomatorHelpers.findByPartialText(VpnProtocols.WireGuard.name))
        assertTrue(true)
    }
}
