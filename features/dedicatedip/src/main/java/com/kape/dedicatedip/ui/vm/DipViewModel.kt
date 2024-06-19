package com.kape.dedicatedip.ui.vm

import android.app.Activity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.FetchSignupDipToken
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.ObserveScreenCaptureUseCase
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
    private val observeScreenCaptureUseCase: ObserveScreenCaptureUseCase,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val dipPrefs: DipPrefs,
    private val router: Router,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<DedicatedIpStep?>(null)
    val state: StateFlow<DedicatedIpStep?> = _state
    val activateTokenButtonState = mutableStateOf(false)
    private lateinit var userLocale: String

    val dipList = mutableStateListOf<VpnServer>()
    val activationState = mutableStateOf<DipApiResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(true)
    val supportedDipCountriesList = mutableStateOf<DipCountriesResponse?>(null)
    val dipMonthlyPlan = mutableStateOf<DedicatedIpMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DedicatedIpYearlyPlan?>(null)
    val selectedPlanProductId = mutableStateOf(dipPrefs.getSelectedDipSignupProductId())
    val showSupportedCountriesDialog = mutableStateOf(true)
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

    fun registerScreenCaptureCallback(callback: () -> Unit) {
        observeScreenCaptureUseCase.registerCallback(callback)
    }

    fun loadDedicatedIps(locale: String) = viewModelScope.launch {
        userLocale = locale
        dipList.clear()
        for (dip in dipPrefs.getDedicatedIps()) {
            dipList.addAll(regionListProvider.servers.value.filter { it.isDedicatedIp })
        }
    }

    fun activateDedicatedIp(ipToken: MutableState<TextFieldValue>) = viewModelScope.launch {
        activateDipUseCase.activate(ipToken.value.text).collect {
            activationState.value = it
            when (it) {
                DipApiResult.Active -> {
                    ipToken.value = TextFieldValue("")
                    loadDedicatedIps(userLocale)
                }

                DipApiResult.Expired -> {
                    ipToken.value = TextFieldValue("")
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

    fun removeDip(dipToken: String) {
        for (dip in dipPrefs.getDedicatedIps()) {
            if (dip.dipToken == dipToken) {
                dipPrefs.removeDedicatedIp(dip)
                loadDedicatedIps(userLocale)
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

    private fun navigateToDedicatedIpPurchaseSuccess() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupSuccess)
    }
}