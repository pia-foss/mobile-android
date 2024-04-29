package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.vpn.BuildConfig
import org.junit.Before
import screens.helpers.UiAutomatorStepsHelper
import screens.steps.DedicatedIPSteps
import screens.steps.LoginSteps
import screens.steps.MainScreenSteps
import screens.steps.ProtocolsSteps
import screens.steps.SettingsSteps
import screens.steps.SideMenuSteps
import screens.steps.SignUpSteps

open class UiTest(
    val loginSteps: LoginSteps = LoginSteps,
    val signUpSteps: SignUpSteps = SignUpSteps,
    val mainScreenSteps: MainScreenSteps = MainScreenSteps,
    val sideMenuSteps: SideMenuSteps = SideMenuSteps,
    val settingsSteps: SettingsSteps = SettingsSteps,
    val protocolSteps: ProtocolsSteps = ProtocolsSteps,
    val dedicatedIPSteps: DedicatedIPSteps = DedicatedIPSteps,
) {

    var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    var intent: Intent? =
        context.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)

    init {
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    }

    @Before
    open fun setUp() {
        context.startActivity(intent)
        UiAutomatorStepsHelper.waitUntilFound(signUpSteps.loginButton)
        signUpSteps.loginButton.clickAndWaitForNewWindow()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()
    }

    @Before
    fun setupWithoutLogin() {
        context.startActivity(intent)
    }
}
