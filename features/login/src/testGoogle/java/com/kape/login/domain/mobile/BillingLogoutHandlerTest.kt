package com.kape.login.domain.mobile

import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.KpiPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.payments.prefs.SubscriptionPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BillingLogoutHandlerTest {
    private val connectionPrefs: ConnectionPrefs = mockk()
    private val csiPrefs: CsiPrefs = mockk()
    private val customizationPrefs: CustomizationPrefs = mockk()
    private val dipPrefs: DipPrefs = mockk()
    private val networkManagementPrefs: NetworkManagementPrefs = mockk()
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk()
    private val shortcutPrefs: ShortcutPrefs = mockk()
    private val vpnRegionPrefs: VpnRegionPrefs = mockk()
    private val settingsPrefs: SettingsPrefs = mockk()
    private val kpiPrefs: KpiPrefs = mockk()
    private val consentPrefs: ConsentPrefs = mockk()
    private val ratingPrefs: RatingPrefs = mockk()
    private val subscriptionPrefs: SubscriptionPrefs = mockk()

    private lateinit var handler: BillingLogoutHandler

    @BeforeEach
    fun setUp() {
        handler =
            BillingLogoutHandler(
                connectionPrefs,
                csiPrefs,
                customizationPrefs,
                dipPrefs,
                networkManagementPrefs,
                shadowsocksRegionPrefs,
                shortcutPrefs,
                vpnRegionPrefs,
                settingsPrefs,
                kpiPrefs,
                consentPrefs,
                ratingPrefs,
                subscriptionPrefs,
            )

        coEvery { connectionPrefs.clear() } returns Unit
        coEvery { csiPrefs.clear() } returns Unit
        coEvery { customizationPrefs.clear() } returns Unit
        coEvery { dipPrefs.clear() } returns Unit
        coEvery { networkManagementPrefs.clear() } returns Unit
        coEvery { shadowsocksRegionPrefs.clear() } returns Unit
        coEvery { shortcutPrefs.clear() } returns Unit
        coEvery { vpnRegionPrefs.clear() } returns Unit
        coEvery { settingsPrefs.clear() } returns Unit
        coEvery { kpiPrefs.clear() } returns Unit
        coEvery { consentPrefs.clearAllowSharing() } returns Unit
        coEvery { ratingPrefs.clear() } returns Unit
        coEvery { subscriptionPrefs.clear() } returns Unit
    }

    @Test
    fun `test clearLocalStorage preserves the consent decision but clears the analytics opt-in and subscription data`() =
        runTest {
            handler.clearLocalStorage()

            // Must NOT call the blanket clear() on ConsentPrefs - that would also wipe
            // hasMadeConsentDecision and re-show the consent screen after logout (KM-17672).
            coVerify(exactly = 0) { consentPrefs.clear() }
            coVerify(exactly = 1) { consentPrefs.clearAllowSharing() }

            // The purchase record must be cleared on logout too, so it can't be mistaken
            // for a still-pending purchase on a future signup attempt.
            coVerify(exactly = 1) { subscriptionPrefs.clear() }

            coVerify(exactly = 1) { connectionPrefs.clear() }
            coVerify(exactly = 1) { csiPrefs.clear() }
            coVerify(exactly = 1) { customizationPrefs.clear() }
            coVerify(exactly = 1) { dipPrefs.clear() }
            coVerify(exactly = 1) { networkManagementPrefs.clear() }
            coVerify(exactly = 1) { shadowsocksRegionPrefs.clear() }
            coVerify(exactly = 1) { shortcutPrefs.clear() }
            coVerify(exactly = 1) { vpnRegionPrefs.clear() }
            coVerify(exactly = 1) { settingsPrefs.clear() }
            coVerify(exactly = 1) { kpiPrefs.clear() }
            coVerify(exactly = 1) { ratingPrefs.clear() }
        }
}