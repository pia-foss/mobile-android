package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.kape.vpn.BuildConfig
import org.junit.Before
import screens.steps.UiAutomatorSignInSteps
import tests.actions.UiAction

open class UiAutomatorTest : UiTest {

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var context: Context

    override val uiAction: UiAction = UiAction(UiAutomatorSignInSteps())

    private fun startApp(packageName: String) {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
        context.startActivity(intent)
    }

    @Before
    override fun setUp() {
        startApp(BuildConfig.APPLICATION_ID)
    }

    companion object {
        const val defaultTimeOut = 5000L
    }
}