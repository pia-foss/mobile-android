package com.kape.dedicatedip.ui.vm

import android.app.Activity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.DedicatedIpLocationSelection
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.DedicatedIpSignupTokenActivate
import com.kape.data.DedicatedIpSignupTokenDetails
import com.kape.data.dip.DipPurchaseData
import com.kape.data.vpnserver.VpnServer
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.DipPurchaseHandler
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.data.DedicatedIpSelectedCountry
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.vpnregions.utils.RegionListProvider
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class DipViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val activateDipUseCase: ActivateDipUseCase,
    private val dipPrefs: DipPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val buildConfigProvider: BuildConfigProvider,
    private val connectionManager: ConnectionManager,
    private val dipPurchaseHandler: DipPurchaseHandler,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow<DedicatedIpStep?>(null)
    val state: StateFlow<DedicatedIpStep?> = _state
    val activateTokenButtonState = mutableStateOf(false)
    private val _dipList = MutableStateFlow<List<VpnServer>>(emptyList())
    val dipList = _dipList.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            dipPrefs.dedicatedIps.collect {
                println("--- prefs updated: $it")
                regionListProvider.reflectDedicatedIpAction()
                loadDedicatedIps()
            }
        }
    }

    val activationState = mutableStateOf<DipApiResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(true)
    val supportedDipCountriesList = mutableStateOf<DipCountriesResponse?>(null)
    val dipMonthlyPlan = mutableStateOf<DedicatedIpMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DedicatedIpYearlyPlan?>(null)
    val selectedPlanProductId = dipPrefs.selectedDipSignupProductId
    val showSupportedCountriesDialog = mutableStateOf(false)
    val showFetchingNeededInformationError = mutableStateOf(false)
    val showPurchaseValidationError = mutableStateOf(false)
    val showTokenRetrievalError = mutableStateOf(false)
    val showFetchingPlansSpinner = mutableStateOf(true)
    val showSpinner = mutableStateOf(false)

    fun navigateToActivateToken() = router.updateDestination(DedicatedIpActivateToken)

    fun navigateToDedicatedIpPlans() = router.updateDestination(DedicatedIpSignupPlans)

    fun navigateToDedicatedIpTokenDetails() = router.updateDestination(DedicatedIpSignupTokenDetails)

    fun navigateToDedicatedIpTokenActivate() = router.updateDestination(DedicatedIpSignupTokenActivate)

    fun navigateToDedicatedIpLocationSelection() = router.updateDestination(DedicatedIpLocationSelection)

    fun navigateBack() = router.navigateBack()

    private fun loadDedicatedIps() =
        viewModelScope.launch(ioDispatcher) {
            _dipList.update { regionListProvider.servers.first().filter { it.isDedicatedIp } }
        }

    fun activateDedicatedIp(dipToken: MutableState<TextFieldValue>) =
        viewModelScope.launch(ioDispatcher) {
            val result = activateDipUseCase.activate(dipToken.value.text)
            println("--- activation state result: $result")
            activationState.value = result
            when (result) {
                DipApiResult.Active -> {
                    dipToken.value = TextFieldValue("")
                    dipPrefs.removePurchasedSignupDipToken()
                }

                DipApiResult.Expired -> {
                    dipToken.value = TextFieldValue("")
                }

                DipApiResult.Error,
                DipApiResult.Invalid,
                -> {
                    // no-op
                }
            }
        }

    fun resetActivationState() {
        activationState.value = null
    }

    fun removeDip(
        serverKey: String,
        dipToken: String,
    ) = viewModelScope.launch(ioDispatcher) {
        val dip = dipPrefs.dedicatedIps.value.firstOrNull { it.dipToken == dipToken }
        dip?.let {
            dipPrefs.removeDedicatedIp(it)
            connectionPrefs.removeFromQuickConnect(serverKey)
            if (connectionPrefs.selectedVpnServer.first()?.key == serverKey) {
                connectionPrefs.setSelectedVpnServer(null)
                disconnect()
            }
        }
    }

    fun getDipSupportedCountries() =
        viewModelScope.launch(ioDispatcher) {
            val response = dipPurchaseHandler.getDipSupportedCountries()
            if (response == null || response.dedicatedIpCountriesAvailable.isEmpty()) {
                showSupportedCountriesDialog.value = false
                showFetchingNeededInformationError.value = true
                return@launch
            }

            supportedDipCountriesList.value = response
            val selectedDipCountry =
                getSelectedDipCountry().first() ?: DedicatedIpSelectedCountry(
                    countryCode = response.dedicatedIpCountriesAvailable.first().countryCode,
                    countryName = response.dedicatedIpCountriesAvailable.first().name,
                    regionName =
                        response.dedicatedIpCountriesAvailable
                            .first()
                            .regions
                            .first(),
                )
            selectDipCountry(selectedDipCountry)
        }

    fun getDipMonthlyPlan() =
        viewModelScope.launch(ioDispatcher) {
            val monthlyPlan = dipPurchaseHandler.getDipMonthlyPlan()
            showFetchingNeededInformationError.value =
                dipYearlyPlan.value == null &&
                monthlyPlan == null
            monthlyPlan?.let {
                dipMonthlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnacknowledgedDipPurchases(productId = it.id)
            }
        }

    fun getDipYearlyPlan() =
        viewModelScope.launch(ioDispatcher) {
            val yearlyPlan = dipPurchaseHandler.getDipYearlyPlan()
            showFetchingNeededInformationError.value =
                dipMonthlyPlan.value == null &&
                yearlyPlan == null
            yearlyPlan?.let {
                if (dipPrefs.selectedDipSignupProductId.value.isEmpty()) {
                    selectPlanProductId(it.id)
                }
                dipYearlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnacknowledgedDipPurchases(productId = it.id)
            }
        }

    fun hasActivePlaystoreSubscription() =
        viewModelScope.launch(ioDispatcher) {
            dipPurchaseHandler.hasActiveSubscription().collect {
                hasAnActivePlaystoreSubscription.value = it
            }
        }

    fun showDedicatedIpSignupBanner() = dipPrefs.isDipSignupEnabled(buildConfigProvider.isGoogleFlavor())

    fun selectDipCountry(dedicatedIpSelectedCountry: DedicatedIpSelectedCountry) =
        viewModelScope.launch(ioDispatcher) {
            dipPrefs.setDedicatedIpSelectedCountry(dedicatedIpSelectedCountry = dedicatedIpSelectedCountry)
        }

    fun getSelectedDipCountry() = dipPrefs.dedicatedIpSelectedCountry

    fun selectPlanProductId(productId: String) =
        viewModelScope.launch(ioDispatcher) {
            dipPrefs.setSelectedDipSignupProductId(productId)
        }

    fun enableActivateTokenButton() {
        activateTokenButtonState.value = true
    }

    fun getSignupDipToken() = dipPrefs.purchasedSignupDipToken

    fun purchaseSubscription(activity: Activity) {
        viewModelScope.launch(ioDispatcher) {
            if (dipPrefs.selectedDipSignupProductId.first().isEmpty()) {
                return@launch
            }

            dipPurchaseHandler.purchaseProduct(
                activity = activity,
                productId = dipPrefs.selectedDipSignupProductId.first(),
            ) { result ->
                result.fold(
                    onSuccess = {
                        validateSubscriptionPurchase(dipPurchaseData = it)
                    },
                    onFailure = {
                        // Do nothing. The billing library shows the payment error to the users.
                    },
                )
            }
        }
    }

    fun validateSubscriptionPurchase(dipPurchaseData: DipPurchaseData? = null) =
        viewModelScope.launch(ioDispatcher) {
            showSpinner.value = true
            showPurchaseValidationError.value = false
            val result = dipPurchaseHandler.validateDip(dipPurchaseData = dipPurchaseData)
            result.fold(
                onSuccess = {
                    showSpinner.value = false
                    showPurchaseValidationError.value = false
                    fetchPurchasedDedicatedIpToken()
                },
                onFailure = {
                    showPurchaseValidationError.value = true
                },
            )
        }

    fun fetchPurchasedDedicatedIpToken() =
        viewModelScope.launch(ioDispatcher) {
            showSpinner.value = true
            showTokenRetrievalError.value = false
            val countryDetails = getSelectedDipCountry().first()
            if (countryDetails == null) {
                showTokenRetrievalError.value = true
                return@launch
            }

            val result =
                dipPurchaseHandler.fetchDipToken(
                    countryCode = countryDetails.countryCode,
                    regionName = countryDetails.regionName,
                )
            result.fold(
                onSuccess = {
                    showSpinner.value = false
                    showTokenRetrievalError.value = false
                    dipPrefs.setPurchasedSignupDipToken(it)
                    navigateToDedicatedIpPurchaseSuccess()
                },
                onFailure = {
                    showTokenRetrievalError.value = true
                },
            )
        }

    fun resumePossibleUnacknowledgedDipPurchases() =
        viewModelScope.launch(ioDispatcher) {
            val monthlyPlan = dipPurchaseHandler.getDipMonthlyPlan()
            val yearlyPlan = dipPurchaseHandler.getDipYearlyPlan()
            val productIds = mutableListOf<String>()
            monthlyPlan?.let {
                productIds.add(it.id)
            }
            yearlyPlan?.let {
                productIds.add(it.id)
            }

            if (productIds.isEmpty().not()) {
                dipPurchaseHandler.unacknowledgedProductIds(productIds) { result ->
                    result.fold(
                        onSuccess = {
                            if (it.isEmpty().not()) {
                                validateSubscriptionPurchase()
                            }
                        },
                        onFailure = {
                            // Do nothing.
                        },
                    )
                }
            }
        }

    private fun checkForUnacknowledgedDipPurchases(productId: String) =
        viewModelScope.launch(ioDispatcher) {
            dipPurchaseHandler.unacknowledgedProductIds(
                productIds = listOf(productId),
            ) { result ->
                result.fold(
                    onSuccess = {
                        showSupportedCountriesDialog.value = it.isEmpty()
                        if (it.isEmpty().not()) {
                            navigateToDedicatedIpLocationSelection()
                        }
                    },
                    onFailure = {
                        // Do nothing.
                    },
                )
            }
        }

    private fun disconnect() = viewModelScope.launch(ioDispatcher) { connectionManager.disconnect().getOrNull() }

    private fun navigateToDedicatedIpPurchaseSuccess() =
        viewModelScope.launch(ioDispatcher) {
            _state.emit(DedicatedIpStep.SignupSuccess)
        }
}