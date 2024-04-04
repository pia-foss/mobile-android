package tests

import com.kape.settings.data.VpnProtocols
import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.SignUpUiObjects
import kotlin.test.Ignore
import kotlin.test.assertEquals

class UiLogoutTests : UiTest() {
    @Test
    fun sign_out_from_connect_screen_reaches_signup_screen() {
        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiLogoutAction.logout()
        assert(SignUpUiObjects.loginButton.exists())
    }

    @Ignore ("Ignoring because the app will perpetually load as the emulator is not signed in in google playstore ")
    @Test
    fun persistence_layer_wiped_after_sign_out() {
        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiSettingsAction.openSideMenu()
        uiSettingsAction.selectOpenVpnProtocol()
        uiLogoutAction.logout()

        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiSettingsAction.openSideMenu()
        // WireGuard is the default protocol. On logout we clear all settings
        // If the protocol is set back to WireGuard it means that the persistance layer
        // was successfully cleared
        assertEquals(VpnProtocols.WireGuard.name, uiSettingsAction.selectedProtocol())
    }
}