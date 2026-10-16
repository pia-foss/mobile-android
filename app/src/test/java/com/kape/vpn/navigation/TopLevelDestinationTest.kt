package com.kape.vpn.navigation

import androidx.navigation3.runtime.NavKey
import com.kape.data.AccountDeleted
import com.kape.data.Connection
import com.kape.data.LoginWithCredentials
import com.kape.data.ProtocolSettings
import com.kape.data.Settings
import com.kape.data.Splash
import com.kape.data.VpnRegionSelection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TopLevelDestinationTest {
    @Test
    fun `showsNavigation - before login - is false`() {
        assertFalse(TopLevelDestination.showsNavigation(listOf<NavKey>(Splash, LoginWithCredentials)))
    }

    @Test
    fun `showsNavigation - empty back stack - is false`() {
        assertFalse(TopLevelDestination.showsNavigation(emptyList()))
    }

    @Test
    fun `showsNavigation - on connection and its sub-screens - is true`() {
        assertTrue(TopLevelDestination.showsNavigation(listOf<NavKey>(Connection)))
        assertTrue(TopLevelDestination.showsNavigation(listOf<NavKey>(Connection, VpnRegionSelection)))
    }

    @Test
    fun `showsNavigation - excluded screen on top - is false`() {
        assertFalse(TopLevelDestination.showsNavigation(listOf<NavKey>(Connection, AccountDeleted)))
    }

    @Test
    fun `selectedFor - sub-screen - selects nearest top-level parent`() {
        assertEquals(
            TopLevelDestination.SETTINGS,
            TopLevelDestination.selectedFor(listOf<NavKey>(Connection, Settings, ProtocolSettings)),
        )
    }

    @Test
    fun `selectedFor - non top-level screen above connection - selects vpn`() {
        assertEquals(
            TopLevelDestination.VPN,
            TopLevelDestination.selectedFor(listOf<NavKey>(Connection, VpnRegionSelection)),
        )
    }

    @Test
    fun `selectedFor - no top-level entry - is null`() {
        assertNull(TopLevelDestination.selectedFor(listOf<NavKey>(Splash)))
    }
}