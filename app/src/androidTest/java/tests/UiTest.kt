package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.vpn.BuildConfig
import org.junit.Before
import screens.steps.UiAutomatorLogOutSteps
import screens.steps.UiAutomatorSignInSteps
import tests.actions.UiAction

open class UiTest(val uiAction: UiAction = UiAction(UiAutomatorSignInSteps(), UiAutomatorLogOutSteps())) {
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