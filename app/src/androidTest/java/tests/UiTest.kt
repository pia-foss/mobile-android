package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.vpn.BuildConfig
import org.junit.Before
import screens.steps.UiAutomatorCommonSteps
import screens.steps.UiAutomatorProtocolsSteps
import screens.steps.UiAutomatorSettingsSteps
import screens.steps.UiAutomatorSideMenuSteps
import screens.steps.UiAutomatorSignInSteps
import tests.actions.UiLogoutAction
import tests.actions.UiSettingsAction
import tests.actions.UiSignInAction

open class UiTest(
    val uiSignInAction: UiSignInAction = UiSignInAction(UiAutomatorSignInSteps()),
    val uiLogoutAction: UiLogoutAction = UiLogoutAction(
        UiAutomatorCommonSteps(),
        UiAutomatorSideMenuSteps(),
    ),
    val uiSettingsAction: UiSettingsAction = UiSettingsAction(
        UiAutomatorSideMenuSteps(),
        UiAutomatorSettingsSteps(),
        UiAutomatorProtocolsSteps(),
    ),
) {
    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var intent: Intent? =
        context.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)

    init {
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    }

    @Before
    fun setUp() {
        context.startActivity(intent)
    }
}