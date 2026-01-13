package com.kape.dedicatedip.ui.vm

import android.app.Activity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.FetchSignupDipToken
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.ValidateDipSignup
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.dip.data.DedicatedIpSelectedCountry
import com.kape.payments.data.DipPurchaseData
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.router.Back
import com.kape.router.Router
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.RegionListProvider
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class DipViewModel(
    private val regionListProvider: RegionListProvider,
    private val activateDipUseCase: ActivateDipUseCase,
    private val getDipSupportedCountries: GetDipSupportedCountries,
    private val getDipMonthlyPlan: GetDipMonthlyPlan,
    private val getDipYearlyPlan: GetDipYearlyPlan,
    private val validateDipSignup: ValidateDipSignup,
    private val fetchSignupDipToken: FetchSignupDipToken,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val dipPrefs: DipPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val router: Router,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<DedicatedIpStep?>(null)
    val state: StateFlow<DedicatedIpStep?> = _state
    val activateTokenButtonState = mutableStateOf(false)

    val dipList = mutableStateListOf<VpnServer>()
    val activationState = mutableStateOf<DipApiResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(true)
    val supportedDipCountriesList = mutableStateOf<DipCountriesResponse?>(null)
    val dipMonthlyPlan = mutableStateOf<DedicatedIpMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DedicatedIpYearlyPlan?>(null)
    val selectedPlanProductId = mutableStateOf(dipPrefs.getSelectedDipSignupProductId())
    val showSupportedCountriesDialog = mutableStateOf(false)
    val showFetchingNeededInformationError = mutableStateOf(false)
    val showPurchaseValidationError = mutableStateOf(false)
    val showTokenRetrievalError = mutableStateOf(false)
    val showFetchingPlansSpinner = mutableStateOf(true)
    val showSpinner = mutableStateOf(false)

    fun navigateBack() {
        _state.value?.let {
            when (it) {
                DedicatedIpStep.ActivateToken,
                DedicatedIpStep.SignupPlans,
                -> router.handleFlow(Back)

                DedicatedIpStep.LocationSelection,
                -> _state.value = DedicatedIpStep.SignupPlans

                DedicatedIpStep.SignupSuccess,
                DedicatedIpStep.SignupTokenDetails,
                DedicatedIpStep.SignupTokenActivate,
                -> {
                    // No-op
                }
            }
        } ?: run {
            router.handleFlow(Back)
        }
    }

    fun navigateToActivateToken() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.ActivateToken)
    }

    fun navigateToDedicatedIpPlans() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupPlans)
    }

    fun navigateToDedicatedIpTokenDetails() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupTokenDetails)
    }

    fun navigateToDedicatedIpTokenActivate() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupTokenActivate)
    }

    fun navigateToDedicatedIpLocationSelection() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.LocationSelection)
    }

    fun loadDedicatedIps() = viewModelScope.launch {
        dipList.clear()
        for (dip in dipPrefs.getDedicatedIps()) {
            dipList.addAll(regionListProvider.servers.value.filter { it.isDedicatedIp })
        }
    }

    fun activateDedicatedIp(dipToken: MutableState<TextFieldValue>) = viewModelScope.launch {
        activateDipUseCase.activate(dipToken.value.text).collect {
            activationState.value = it
            when (it) {
                DipApiResult.Active -> {
                    dipToken.value = TextFieldValue("")
                    dipPrefs.removePurchasedSignupDipToken()
                    loadDedicatedIps()
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
            regionListProvider.reflectDedicatedIpAction()
        }
    }

    fun resetActivationState() {
        activationState.value = null
    }

    fun removeDip(serverKey: String, dipToken: String) {
        for (dip in dipPrefs.getDedicatedIps()) {
            if (dip.dipToken == dipToken) {
                dipPrefs.removeDedicatedIp(dip)
                connectionPrefs.removeFromQuickConnect(serverKey)
                loadDedicatedIps()
            }
        }
    }

    fun getDipSupportedCountries() = viewModelScope.launch {
        getDipSupportedCountries.invoke().collect { response ->
            if (response == null || response.dedicatedIpCountriesAvailable.isEmpty()) {
                showSupportedCountriesDialog.value = false
                showFetchingNeededInformationError.value = true
                return@collect
            }

            supportedDipCountriesList.value = response
            val selectedDipCountry = getSelectedDipCountry() ?: DedicatedIpSelectedCountry(
                countryCode = response.dedicatedIpCountriesAvailable.first().countryCode,
                countryName = response.dedicatedIpCountriesAvailable.first().name,
                regionName = response.dedicatedIpCountriesAvailable.first().regions.first(),
            )
            selectDipCountry(selectedDipCountry)
        }
    }

    fun getDipMonthlyPlan() = viewModelScope.launch {
        getDipMonthlyPlan.invoke().collect { monthlyPlan ->
            showFetchingNeededInformationError.value =
                dipYearlyPlan.value == null && monthlyPlan == null
            monthlyPlan?.let {
                dipMonthlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnacknowledgedDipPurchases(productId = it.id)
            }
        }
    }

    fun getDipYearlyPlan() = viewModelScope.launch {
        getDipYearlyPlan.invoke().collect { yearlyPlan ->
            showFetchingNeededInformationError.value =
                dipMonthlyPlan.value == null && yearlyPlan == null
            yearlyPlan?.let {
                if (dipPrefs.getSelectedDipSignupProductId().isEmpty()) {
                    selectPlanProductId(it.id)
                }
                dipYearlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnacknowledgedDipPurchases(productId = it.id)
            }
        }
    }

    fun hasActivePlaystoreSubscription() = viewModelScope.launch {
        vpnSubscriptionPaymentProvider.hasActiveSubscription().collect {
            hasAnActivePlaystoreSubscription.value = it
        }
    }

    fun showDedicatedIpSignupBanner() =
        dipPrefs.isDipSignupEnabled()

    fun selectDipCountry(dedicatedIpSelectedCountry: DedicatedIpSelectedCountry) {
        dipPrefs.setDedicatedIpSelectedCountry(dedicatedIpSelectedCountry = dedicatedIpSelectedCountry)
    }

    fun getSelectedDipCountry(): DedicatedIpSelectedCountry? =
        dipPrefs.getDedicatedIpSelectedCountry()

    fun selectPlanProductId(productId: String) {
        dipPrefs.setSelectedDipSignupProductId(productId)
        selectedPlanProductId.value = dipPrefs.getSelectedDipSignupProductId()
    }

    fun enableActivateTokenButton() {
        activateTokenButtonState.value = true
    }

    fun getSignupDipToken(): String =
        dipPrefs.getPurchasedSignupDipToken()

    fun purchaseSubscription(activity: Activity) {
        if (dipPrefs.getSelectedDipSignupProductId().isEmpty()) {
            return
        }

        dipSubscriptionPaymentProvider.purchaseProduct(
            activity = activity,
            productId = dipPrefs.getSelectedDipSignupProductId(),
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

    fun validateSubscriptionPurchase(dipPurchaseData: DipPurchaseData? = null) = viewModelScope.launch {
        showSpinner.value = true
        showPurchaseValidationError.value = false
        validateDipSignup.invoke(dipPurchaseData = dipPurchaseData).collect { result ->
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
    }

    fun fetchPurchasedDedicatedIpToken() = viewModelScope.launch {
        showSpinner.value = true
        showTokenRetrievalError.value = false
        val countryDetails = getSelectedDipCountry()
        if (countryDetails == null) {
            showTokenRetrievalError.value = true
            return@launch
        }

        fetchSignupDipToken.invoke(
            countryCode = countryDetails.countryCode,
            regionName = countryDetails.regionName,
        ).collect { result ->
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
    }

    fun resumePossibleUnacknowledgedDipPurchases() = viewModelScope.launch {
        getDipMonthlyPlan.invoke().collect { monthlyPlan ->
            getDipYearlyPlan.invoke().collect { yearlyPlan ->
                val productIds = mutableListOf<String>()
                monthlyPlan?.let {
                    productIds.add(it.id)
                }
                yearlyPlan?.let {
                    productIds.add(it.id)
                }

                if (productIds.isEmpty().not()) {
                    dipSubscriptionPaymentProvider.unacknowledgedProductIds(productIds) { result ->
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
        }
    }

    private fun checkForUnacknowledgedDipPurchases(productId: String) = viewModelScope.launch {
        dipSubscriptionPaymentProvider.unacknowledgedProductIds(
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

    private fun navigateToDedicatedIpPurchaseSuccess() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupSuccess)
    }
}