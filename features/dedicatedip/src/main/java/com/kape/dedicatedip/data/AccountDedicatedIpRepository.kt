package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DedicatedIpRepository
import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.dedicatedip.domain.DipSignupStateRepository
import com.kape.dedicatedip.domain.ServerSelectionRepository
import com.kape.dip.data.DedicatedIpSelectedCountry
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [DedicatedIpRepository::class])
internal class AccountDedicatedIpRepository(
    private val api: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DedicatedIpRepository {
    override suspend fun activate(token: String): DipActivationResult {
        val dip = redeem(token) ?: return DipActivationResult.Error
        return when (dip.status) {
            DedicatedIPInformationResponse.Status.active -> {
                dipPrefs.addDedicatedIp(dip)
                DipActivationResult.Active
            }
            DedicatedIPInformationResponse.Status.expired -> DipActivationResult.Expired
            DedicatedIPInformationResponse.Status.invalid -> DipActivationResult.Invalid
            DedicatedIPInformationResponse.Status.error -> DipActivationResult.Error
        }
    }

    override suspend fun remove(dipToken: String): Boolean {
        val dip = dipPrefs.getDedicatedIpsNow().firstOrNull { it.dipToken == dipToken } ?: return false
        dipPrefs.removeDedicatedIp(dip)
        return true
    }

    private suspend fun redeem(token: String): DedicatedIPInformation? =
        suspendCancellableCoroutine { continuation ->
            api.redeemDedicatedIPs(listOf(token)) { details, errors ->
                continuation.resume(if (errors.isNotEmpty()) null else details.firstOrNull { it.dipToken == token })
            }
        }
}

@Singleton(binds = [ServerSelectionRepository::class])
internal class PrefsServerSelectionRepository(
    private val connectionPrefs: ConnectionPrefs,
) : ServerSelectionRepository {
    override suspend fun selectedServerKey(): String? = connectionPrefs.getSelectedVpnServerNow()?.key

    override suspend fun clearSelectedServer() = connectionPrefs.setSelectedVpnServer(null)

    override suspend fun removeFromQuickConnect(serverKey: String) = connectionPrefs.removeFromQuickConnect(serverKey)
}

@Singleton(binds = [DipSignupStateRepository::class])
internal class PrefsDipSignupStateRepository(
    private val dipPrefs: DipPrefs,
) : DipSignupStateRepository {
    override val selectedCountry: StateFlow<DedicatedIpSelectedCountry?> = dipPrefs.dedicatedIpSelectedCountry

    override suspend fun selectedCountryNow(): DedicatedIpSelectedCountry? = dipPrefs.getDedicatedIpSelectedCountryNow()

    override suspend fun selectCountry(country: DedicatedIpSelectedCountry) = dipPrefs.setDedicatedIpSelectedCountry(country)

    override val selectedProductId: StateFlow<String> = dipPrefs.selectedDipSignupProductId

    override suspend fun selectProduct(productId: String) = dipPrefs.setSelectedDipSignupProductId(productId)

    override val purchasedToken: StateFlow<String> = dipPrefs.purchasedSignupDipToken

    override suspend fun setPurchasedToken(token: String) = dipPrefs.setPurchasedSignupDipToken(token)

    override suspend fun clearPurchasedToken() = dipPrefs.removePurchasedSignupDipToken()

    override fun isSignupEnabled(isGooglePlayBuild: Boolean): Flow<Boolean> = dipPrefs.isDipSignupEnabled(isGooglePlayBuild)
}