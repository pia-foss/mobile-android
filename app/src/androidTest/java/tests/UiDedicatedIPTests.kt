package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.objects.DedicatedIPObjects

class UiDedicatedIPTests : UiTest() {

    @Test
    fun accept_valid_dedicated_ip_token() {
        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiDedicatedIPAction.selectDedicatedIPFromSideMenu()
        uiDedicatedIPAction.activateDedicatedIP(BuildConfig.PIA_VALID_DIP_TOKEN)
        assert(DedicatedIPObjects.dedicatedIPServerName.exists()
                && DedicatedIPObjects.dedicatedIPFlag.exists()
                && !DedicatedIPObjects.dedicatedIPField.exists())
    }

    @Test
    fun reject_invalid_dedicated_ip_token() {
        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiDedicatedIPAction.selectDedicatedIPFromSideMenu()
        uiDedicatedIPAction.activateDedicatedIP("InvalidToken")
        assert(DedicatedIPObjects.dedicatedIPField.exists())
    }

    @Test
    fun reject_empty_dedicated_ip_token() {
        uiSignInAction.signIn(
            BuildConfig.PIA_VALID_USERNAME,
            BuildConfig.PIA_VALID_PASSWORD,
        )
        uiDedicatedIPAction.selectDedicatedIPFromSideMenu()
        uiDedicatedIPAction.activateDedicatedIP("")
        assert(DedicatedIPObjects.dedicatedIPField.exists())
    }
}
