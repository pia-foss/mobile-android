package tests

import com.kape.vpn.BuildConfig
import org.junit.Test
import screens.helpers.UiAutomatorStepsHelper.waitUntilFound

class UiDedicatedIPTests : UiTest() {

    @Test
    fun accept_valid_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN)
        waitUntilFound(dedicatedIPSteps.dedicatedIPServerName)
        assert(dedicatedIPSteps.dedicatedIPServerName.exists()
                && dedicatedIPSteps.dedicatedIPFlag.exists()
                && !dedicatedIPSteps.dedicatedIPField.exists())
    }

    @Test
    fun reject_invalid_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("InvalidToken")
        assert(dedicatedIPSteps.dedicatedIPField.exists())
    }

    @Test
    fun reject_empty_dedicated_ip_token() {
        dedicatedIPSteps.navigateToDedicatedIPPage()
        dedicatedIPSteps.activateDedicatedIPToken("")
        assert(dedicatedIPSteps.dedicatedIPField.exists())
    }
}
