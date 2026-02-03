package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.vpn.BuildConfig
import screens.helpers.UiAutomatorHelpers
import screens.steps.*
import kotlin.test.assertNotNull

open class UiTest(
    val loginSteps: LoginSteps = LoginSteps,
    val signUpSteps: SignUpSteps = SignUpSteps,
    val mainScreenSteps: MainScreenSteps = MainScreenSteps,
    val sideMenuSteps: SideMenuSteps = SideMenuSteps,
    val settingsSteps: SettingsSteps = SettingsSteps,
    val protocolSteps: ProtocolsSteps = ProtocolsSteps,
    val dedicatedIPSteps: DedicatedIPSteps = DedicatedIPSteps,
    val regionSelectionSteps: RegionSelectionSteps = RegionSelectionSteps
) {

    var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var intent: Intent? = context.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)

    init {
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear previous app instances
    }

    /**
     * Default setup: launches app and logs in with valid credentials.
     */
    @org.junit.Before
    open fun setUp() {
        context.startActivity(intent)
        // Wait for SignUp screen
        assertNotNull(UiAutomatorHelpers.findByResId(signUpSteps.loginButton))
        // Navigate to login
        UiAutomatorHelpers.findByResId(signUpSteps.loginButton)?.click()
        loginSteps.logIn(BuildConfig.PIA_VALID_USERNAME, BuildConfig.PIA_VALID_PASSWORD)
        loginSteps.giveAppPermissions()
    }

    /**
     * Setup variant: launches app without logging in.
     * Useful for tests that start from SignUp screen.
     */
    @org.junit.Before
    fun setupWithoutLogin() {
        context.startActivity(intent)
    }
}
