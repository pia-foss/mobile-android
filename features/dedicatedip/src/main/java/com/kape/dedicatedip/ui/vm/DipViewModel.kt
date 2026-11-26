package com.kape.dedicatedip.ui.vm

import android.app.Activity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.DedicatedIpLocationSelection
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.DedicatedIpSignupTokenActivate
import com.kape.data.DedicatedIpSignupTokenDetails
import com.kape.data.model.DipPurchaseData
import com.kape.data.vpnserver.VpnServer
import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.dedicatedip.domain.DipCountry
import com.kape.dedicatedip.domain.DipMonthlyPlan
import com.kape.dedicatedip.domain.DipPurchaseStore
import com.kape.dedicatedip.domain.DipSignupStateRepository
import com.kape.dedicatedip.domain.DipYearlyPlan
import com.kape.dedicatedip.domain.usecase.ActivateDedicatedIpUseCase
import com.kape.dedicatedip.domain.usecase.FetchPurchasedDipTokenUseCase
import com.kape.dedicatedip.domain.usecase.IsDipSignupAvailableUseCase
import com.kape.dedicatedip.domain.usecase.ObserveDedicatedIpServersUseCase
import com.kape.dedicatedip.domain.usecase.RemoveDedicatedIpUseCase
import com.kape.dedicatedip.domain.usecase.TokenFetchResult
import com.kape.dedicatedip.ui.DedicatedIpStep
import com.kape.dip.data.DedicatedIpSelectedCountry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DipViewModel(
    private val router: Router,
    private val activateDedicatedIp: ActivateDedicatedIpUseCase,
    private val removeDedicatedIp: RemoveDedicatedIpUseCase,
    private val isDipSignupAvailable: IsDipSignupAvailableUseCase,
    private val fetchPurchasedDipToken: FetchPurchasedDipTokenUseCase,
    private val store: DipPurchaseStore,
    private val signupState: DipSignupStateRepository,
    observeDedicatedIpServers: ObserveDedicatedIpServersUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<DedicatedIpStep?>(null)
    val state: StateFlow<DedicatedIpStep?> = _state.asStateFlow()

    val dipList: StateFlow<List<VpnServer>> =
        observeDedicatedIpServers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activateTokenButtonState = mutableStateOf(false)
    val activationState = mutableStateOf<DipActivationResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(true)
    val supportedDipCountriesList = mutableStateOf<List<DipCountry>?>(null)
    val dipMonthlyPlan = mutableStateOf<DipMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DipYearlyPlan?>(null)
    val selectedPlanProductId: StateFlow<String> = signupState.selectedProductId
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

    fun activateDedicatedIp(dipToken: MutableState<TextFieldValue>) =
        viewModelScope.launch {
            val result = activateDedicatedIp(dipToken.value.text)
            activationState.value = result
            if (result == DipActivationResult.Active || result == DipActivationResult.Expired) {
                dipToken.value = TextFieldValue("")
            }
        }

    fun resetActivationState() {
        activationState.value = null
    }

    fun removeDip(
        serverKey: String,
        dipToken: String,
    ) = viewModelScope.launch { removeDedicatedIp(serverKey, dipToken) }

    fun getDipSupportedCountries() =
        viewModelScope.launch {
            val countries = store.supportedCountries()
            if (countries.isNullOrEmpty()) {
                showSupportedCountriesDialog.value = false
                showFetchingNeededInformationError.value = true
                return@launch
            }
            supportedDipCountriesList.value = countries
            val first = countries.first()
            selectDipCountry(
                signupState.selectedCountryNow()
                    ?: DedicatedIpSelectedCountry(
                        countryCode = first.countryCode,
                        countryName = first.name,
                        regionName = first.regions.first(),
                    ),
            )
        }

    fun getDipMonthlyPlan() =
        viewModelScope.launch {
            val monthlyPlan = store.monthlyPlan()
            showFetchingNeededInformationError.value = dipYearlyPlan.value == null && monthlyPlan == null
            monthlyPlan?.let {
                dipMonthlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnregisteredPurchase(it.id)
            }
        }

    fun getDipYearlyPlan() =
        viewModelScope.launch {
            val yearlyPlan = store.yearlyPlan()
            showFetchingNeededInformationError.value = dipMonthlyPlan.value == null && yearlyPlan == null
            yearlyPlan?.let {
                if (selectedPlanProductId.value.isEmpty()) selectPlanProductId(it.id)
                dipYearlyPlan.value = it
                showFetchingPlansSpinner.value = false
                checkForUnregisteredPurchase(it.id)
            }
        }

    fun hasActivePlaystoreSubscription() =
        viewModelScope.launch {
            store.hasActiveVpnSubscription().collect { hasAnActivePlaystoreSubscription.value = it }
        }

    fun showDedicatedIpSignupBanner(): Flow<Boolean> = isDipSignupAvailable()

    fun selectDipCountry(country: DedicatedIpSelectedCountry) = viewModelScope.launch { signupState.selectCountry(country) }

    fun getSelectedDipCountry(): StateFlow<DedicatedIpSelectedCountry?> = signupState.selectedCountry

    fun selectPlanProductId(productId: String) = viewModelScope.launch { signupState.selectProduct(productId) }

    fun enableActivateTokenButton() {
        activateTokenButtonState.value = true
    }

    fun getSignupDipToken(): StateFlow<String> = signupState.purchasedToken

    fun purchaseSubscription(activity: Activity) =
        viewModelScope.launch {
            val productId = selectedPlanProductId.value.ifEmpty { return@launch }
            store.purchase(activity, productId).onSuccess { validateSubscriptionPurchase(it) }
        }

    fun validateSubscriptionPurchase(purchase: DipPurchaseData? = null) =
        viewModelScope.launch {
            showSpinner.value = true
            showPurchaseValidationError.value = false
            val registered = store.registerPurchase(purchase).isSuccess
            showSpinner.value = false
            showPurchaseValidationError.value = !registered
            if (registered) fetchPurchasedDedicatedIpToken()
        }

    fun fetchPurchasedDedicatedIpToken() =
        viewModelScope.launch {
            showSpinner.value = true
            showTokenRetrievalError.value = false
            val result = fetchPurchasedDipToken()
            showSpinner.value = false
            showTokenRetrievalError.value = result != TokenFetchResult.Fetched
            if (result == TokenFetchResult.Fetched) _state.value = DedicatedIpStep.SignupSuccess
        }

    fun resumePossibleUnacknowledgedDipPurchases() =
        viewModelScope.launch {
            val productIds = listOfNotNull(store.monthlyPlan()?.id, store.yearlyPlan()?.id)
            if (productIds.isNotEmpty() && !store.unregisteredPurchases(productIds).isNullOrEmpty()) {
                validateSubscriptionPurchase()
            }
        }

    private suspend fun checkForUnregisteredPurchase(productId: String) {
        val unregistered = store.unregisteredPurchases(listOf(productId)) ?: return
        showSupportedCountriesDialog.value = unregistered.isEmpty()
        if (unregistered.isNotEmpty()) navigateToDedicatedIpLocationSelection()
    }
}