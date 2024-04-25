package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.steps.DedicatedIPSteps

class UiDedicatedIPTests : UiTest() {

    @Test
    fun accept_valid_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)
        assert(DedicatedIPSteps.dedicatedIPServerName.exists()
                && DedicatedIPSteps.dedicatedIPFlag.exists()
                && !DedicatedIPSteps.dedicatedIPField.exists())
    }

    @Test
    fun reject_invalid_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("InvalidToken")
        assert(DedicatedIPSteps.dedicatedIPField.exists())
    }

    @Test
    fun reject_empty_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("")
        assert(DedicatedIPSteps.dedicatedIPField.exists())
    }
}
