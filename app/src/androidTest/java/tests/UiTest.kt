package tests

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.vpn.BuildConfig
import org.junit.Before
import screens.objects.DedicatedIPObjects
import screens.objects.LoginUiObjects
import screens.objects.MainScreenObjects
import screens.objects.ProtocolsObjects
import screens.objects.SettingsObjects
import screens.objects.SideMenuObjects
import screens.objects.SignUpUiObjects

open class UiTest(
    val loginUiObjects: LoginUiObjects = LoginUiObjects,
    val signUpUiObjects: SignUpUiObjects = SignUpUiObjects,
    val mainScreenUiObjects: MainScreenObjects = MainScreenObjects,
    val sideMenuUiObjects: SideMenuObjects = SideMenuObjects,
    val settingsUiObjects: SettingsObjects = SettingsObjects,
    val protocolUiObjects: ProtocolsObjects = ProtocolsObjects,
    val dedicateIPObjects: DedicatedIPObjects = DedicatedIPObjects,
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