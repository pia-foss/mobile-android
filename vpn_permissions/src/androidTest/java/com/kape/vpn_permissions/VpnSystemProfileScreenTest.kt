package com.kape.vpn_permissions

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.kape.uicomponents.theme.PIATheme
import com.kape.vpn_permissions.data.VpnPermissionDataSourceImpl
import com.kape.vpn_permissions.di.permissionModule
import com.kape.vpn_permissions.domain.VpnPermissionDataSource
import com.kape.vpn_permissions.ui.VpnSystemProfileScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.fail

internal class VpnSystemProfileScreenTest : KoinTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var context: Context
    private lateinit var dataSource: VpnPermissionDataSource
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context.applicationContext
        dataSource = VpnPermissionDataSourceImpl(context = context)

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    fun tearDown() {
        stopKoin()
    }

    @Test
    fun grantVpnSystemProfile() {
        startKoin {
            modules(mutableListOf<Module>().apply {
                add(module {
                    single { context }
                })
                add(permissionModule)
            })
        }

        rule.setContent {
            PIATheme {
                VpnSystemProfileScreen()
            }
        }

        if (dataSource.isVpnProfileInstalled()) {
            // the VPN system profile is already granted
            return
        }

        rule.onNodeWithText(text = context.getString(R.string.setup_vpn_profile_ok_button)).performClick()

        rule.waitForIdle()

        device.findObject(By.text("OK")).click()

        rule.waitForIdle()

        if (dataSource.isVpnProfileInstalled().not()) {
            fail("VPN permission not granted")
        }
    }
}