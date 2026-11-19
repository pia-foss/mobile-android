package com.kape.dedicatedip.data

import com.kape.contracts.DedicatedIpHome
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.usecase.IsDipSignupAvailableUseCase
import com.kape.localprefs.prefs.DipPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Singleton

@Singleton(binds = [DedicatedIpHome::class])
internal class DedicatedIpHomeAdapter(
    private val isDipSignupAvailable: IsDipSignupAvailableUseCase,
    private val dipPrefs: DipPrefs,
    private val store: DipPurchaseStore,
) : DedicatedIpHome {
    override fun observeSignupBannerVisible(): Flow<Boolean> =
        combine(isDipSignupAvailable(), dipPrefs.observeHomeBannerVisible()) { available, visible -> available && visible }

    override suspend fun hideSignupBanner() = dipPrefs.hideDedicatedIpHomeBanner()

    override suspend fun renewDedicatedIps() {
        dipPrefs.getDedicatedIpsNow().forEach { store.renew(it.dipToken) }
    }
}