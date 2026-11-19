package com.kape.automation.data

import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.networkmanagement.utils.NetworkUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NetworkRulesManagerTest {
    private val rules = MutableStateFlow<List<NetworkItem>>(emptyList())
    private val prefs =
        mockk<NetworkManagementPrefs>(relaxed = true) {
            every { allRules } returns rules
            coEvery { getAllRulesNow() } answers { rules.value }
            coEvery { addDefaultRule(any()) } answers { rules.value += firstArg<NetworkItem>() }
        }

    private val openWifi =
        NetworkItem("Open WiFi", NetworkType.WifiOpen, NetworkBehavior.AlwaysConnect, isDefaultForOpen = true)
    private val mobileData =
        NetworkItem("Mobile data", NetworkType.MobileData, NetworkBehavior.AlwaysConnect, isDefaultForMobile = true)
    private val home = NetworkItem("home", NetworkType.WifiCustom, NetworkBehavior.AlwaysDisconnect)

    private val util = mockk<NetworkUtil> { every { getDefaultList() } returns listOf(openWifi, mobileData) }
    private val manager = NetworkRulesManager(prefs, util)

    @Test
    fun `adds the default rules when there are none`() =
        runTest {
            assertEquals(listOf(openWifi, mobileData), manager.getRules().first { it.isNotEmpty() })
            coVerify(exactly = 1) { prefs.addDefaultRule(openWifi) }
            coVerify(exactly = 1) { prefs.addDefaultRule(mobileData) }
        }

    @Test
    fun `leaves existing rules alone`() =
        runTest {
            rules.value = listOf(home)

            assertEquals(listOf(home), manager.getRules().first())
            coVerify(exactly = 0) { prefs.addDefaultRule(any()) }
        }

    @Test
    fun `doesn't re-add the defaults while the cached rules are still empty`() =
        runTest {
            rules.value = listOf(openWifi.copy(networkBehavior = NetworkBehavior.AlwaysDisconnect))
            every { prefs.allRules } returns MutableStateFlow(emptyList())

            manager.getRules().first()

            coVerify(exactly = 0) { prefs.addDefaultRule(any()) }
        }

    @Test
    fun `lists the default rules first, keeping the stored order otherwise`() =
        runTest {
            val office = home.copy(networkName = "office")
            rules.value = listOf(home, openWifi, office, mobileData)

            assertEquals(listOf(openWifi, mobileData, home, office), manager.getRules().first())
        }

    @Test
    fun `adding a rule for a known network replaces its rule`() =
        runTest {
            rules.value = listOf(home, openWifi)

            manager.addRule("home", NetworkBehavior.RetainState)

            coVerifyOrder {
                prefs.removeRuleForNetwork(home)
                prefs.addRuleForNetwork("home", NetworkBehavior.RetainState)
            }
        }

    @Test
    fun `replaces a known network's rule even when the cached rules are still empty`() =
        runTest {
            rules.value = listOf(home)
            every { prefs.allRules } returns MutableStateFlow(emptyList())

            manager.addRule("home", NetworkBehavior.RetainState)

            coVerify { prefs.removeRuleForNetwork(home) }
        }

    @Test
    fun `adding a rule for a new network removes nothing`() =
        runTest {
            rules.value = listOf(home)

            manager.addRule("cafe", NetworkBehavior.AlwaysConnect)

            coVerify(exactly = 0) { prefs.removeRuleForNetwork(any()) }
            coVerify { prefs.addRuleForNetwork("cafe", NetworkBehavior.AlwaysConnect) }
        }

    @Test
    fun `updating and removing go straight to the prefs`() =
        runTest {
            manager.updateRule(home, NetworkBehavior.RetainState)
            manager.removeRule(home)

            coVerify { prefs.updateRuleForNetwork(home, NetworkBehavior.RetainState) }
            coVerify { prefs.removeRuleForNetwork(home) }
        }
}