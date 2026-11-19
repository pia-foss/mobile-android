package com.kape.dedicatedip.domain.usecase

import com.kape.contracts.AppInfo
import com.kape.contracts.VpnConnectionControl
import com.kape.data.vpnserver.VpnServer
import com.kape.dedicatedip.domain.DedicatedIpRepository
import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.DipSignupStateRepository
import com.kape.dedicatedip.domain.ServerSelectionRepository
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

private const val GOOGLE_PLAY_FLAVOR = "google"

@Factory
class ObserveDedicatedIpServersUseCase(
    private val observeVpnServers: ObserveVpnServersUseCase,
) {
    operator fun invoke(): Flow<List<VpnServer>> = observeVpnServers().map { servers -> servers.filter { it.isDedicatedIp } }
}

@Factory
class ActivateDedicatedIpUseCase(
    private val repository: DedicatedIpRepository,
    private val signupState: DipSignupStateRepository,
) {
    suspend operator fun invoke(token: String): DipActivationResult =
        repository.activate(token).also { result ->
            if (result == DipActivationResult.Active) signupState.clearPurchasedToken()
        }
}

@Factory
class RemoveDedicatedIpUseCase(
    private val repository: DedicatedIpRepository,
    private val serverSelection: ServerSelectionRepository,
    private val vpnConnection: VpnConnectionControl,
) {
    suspend operator fun invoke(
        serverKey: String,
        dipToken: String,
    ) {
        if (!repository.remove(dipToken)) return
        serverSelection.removeFromQuickConnect(serverKey)
        if (serverSelection.selectedServerKey() == serverKey) {
            serverSelection.clearSelectedServer()
            vpnConnection.disconnect()
        }
    }
}

@Factory
class IsDipSignupAvailableUseCase(
    private val appInfo: AppInfo,
    private val signupState: DipSignupStateRepository,
) {
    operator fun invoke(): Flow<Boolean> = signupState.isSignupEnabled(isGooglePlayBuild = appInfo.buildFlavor == GOOGLE_PLAY_FLAVOR)
}

sealed interface TokenFetchResult {
    data object Fetched : TokenFetchResult

    data object NoCountrySelected : TokenFetchResult

    data object Failed : TokenFetchResult
}

@Factory
class FetchPurchasedDipTokenUseCase(
    private val store: DipPurchaseStore,
    private val signupState: DipSignupStateRepository,
) {
    suspend operator fun invoke(): TokenFetchResult {
        val country = signupState.selectedCountryNow() ?: return TokenFetchResult.NoCountrySelected
        return store.fetchToken(country.countryCode, country.regionName).fold(
            onSuccess = { token ->
                signupState.setPurchasedToken(token)
                TokenFetchResult.Fetched
            },
            onFailure = { TokenFetchResult.Failed },
        )
    }
}