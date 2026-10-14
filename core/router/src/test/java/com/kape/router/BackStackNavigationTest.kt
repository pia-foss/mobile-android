package com.kape.router

import androidx.navigation3.runtime.NavKey
import com.kape.data.AutomationLocation
import com.kape.data.AutomationMain
import com.kape.data.AutomationSettings
import com.kape.data.Connection
import com.kape.data.Consent
import com.kape.data.LoginWithCredentials
import com.kape.data.NotificationPermission
import com.kape.data.Settings
import com.kape.data.Splash
import com.kape.data.VpnPermission
import com.kape.data.VpnRegionSelection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BackStackNavigationTest {
    @Test
    fun `navigateTo - destination without options - is pushed on top`() {
        val backStack = mutableListOf<NavKey>(Connection)

        backStack.navigateTo(Settings)

        assertEquals(listOf(Connection, Settings), backStack)
    }

    @Test
    fun `navigateTo - same destination twice - is pushed twice`() {
        val backStack = mutableListOf<NavKey>(Connection)

        backStack.navigateTo(VpnRegionSelection)
        backStack.navigateTo(VpnRegionSelection)

        assertEquals(listOf(Connection, VpnRegionSelection, VpnRegionSelection), backStack)
    }

    @Test
    fun `navigateTo - ClearAll - leaves only the destination`() {
        val backStack = mutableListOf<NavKey>(Splash, LoginWithCredentials, Settings)

        backStack.navigateTo(Connection)

        assertEquals(listOf(Connection), backStack)
    }

    @Test
    fun `navigateTo - ClearAll to destination with arguments - leaves only the destination`() {
        val backStack = mutableListOf<NavKey>(Connection, Settings)

        backStack.navigateTo(Consent(isFirstScreen = true))

        assertEquals(listOf(Consent(isFirstScreen = true)), backStack)
    }

    @Test
    fun `navigateTo - PopUpTo inclusive - removes the target and everything above it`() {
        val backStack = mutableListOf<NavKey>(Splash, LoginWithCredentials)

        backStack.navigateTo(VpnPermission)

        assertEquals(listOf(VpnPermission), backStack)
    }

    @Test
    fun `navigateTo - PopUpTo exclusive - keeps the target`() {
        val backStack = mutableListOf<NavKey>(Connection, Settings, AutomationSettings, AutomationLocation)

        backStack.navigateTo(AutomationMain)

        assertEquals(listOf(Connection, Settings, AutomationSettings, AutomationMain), backStack)
    }

    @Test
    fun `navigateTo - PopUpTo target missing from the stack - only pushes`() {
        val backStack = mutableListOf<NavKey>(Connection)

        backStack.navigateTo(NotificationPermission)

        assertEquals(listOf(Connection, NotificationPermission), backStack)
    }

    @Test
    fun `navigateTo - PopUpTo with target present twice - pops to the most recent one`() {
        val backStack = mutableListOf<NavKey>(AutomationSettings, Settings, AutomationSettings, AutomationLocation)

        backStack.navigateTo(AutomationMain)

        assertEquals(listOf(AutomationSettings, Settings, AutomationSettings, AutomationMain), backStack)
    }

    @Test
    fun `navigateBack - more than one entry - removes the top`() {
        val backStack = mutableListOf<NavKey>(Connection, Settings)

        val handled = backStack.navigateBack()

        assertTrue(handled)
        assertEquals(listOf(Connection), backStack)
    }

    @Test
    fun `navigateBack - single entry - keeps it`() {
        val backStack = mutableListOf<NavKey>(Connection)

        val handled = backStack.navigateBack()

        assertFalse(handled)
        assertEquals(listOf(Connection), backStack)
    }
}