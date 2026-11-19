package com.kape.login.data

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
import com.kape.login.domain.StoreDataCleaner
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PrefsLocalSessionRepositoryTest {
    private val connection = mockk<ConnectionPrefs>(relaxed = true)
    private val csi = mockk<CsiPrefs>(relaxed = true)
    private val customization = mockk<CustomizationPrefs>(relaxed = true)
    private val dip = mockk<DipPrefs>(relaxed = true)
    private val networkManagement = mockk<NetworkManagementPrefs>(relaxed = true)
    private val shadowsocksRegion = mockk<ShadowsocksRegionPrefs>(relaxed = true)
    private val shortcut = mockk<ShortcutPrefs>(relaxed = true)
    private val vpnRegion = mockk<VpnRegionPrefs>(relaxed = true)
    private val settings = mockk<SettingsPrefs>(relaxed = true)
    private val kpi = mockk<KpiPrefs>(relaxed = true)
    private val consent = mockk<ConsentPrefs>(relaxed = true)
    private val rating = mockk<RatingPrefs>(relaxed = true)
    private val storeData = mockk<StoreDataCleaner>(relaxed = true)

    private val repository =
        PrefsLocalSessionRepository(
            connection,
            csi,
            customization,
            dip,
            networkManagement,
            shadowsocksRegion,
            shortcut,
            vpnRegion,
            settings,
            kpi,
            consent,
            rating,
            storeData,
        )

    @Test
    fun `logging out wipes every stored user setting and the store data`() =
        runTest {
            repository.clear()

            listOf(connection, csi, customization, dip, networkManagement, shadowsocksRegion, shortcut, vpnRegion, settings, kpi, rating)
                .forEach { coVerify { it.clear() } }
            coVerify { consent.clearAllowSharing() }
            coVerify { storeData.clear() }
        }
}