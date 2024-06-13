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
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.GetSignupDipToken
import com.kape.dedicatedip.domain.ValidateDipSignup
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
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
    private val getSignupDipToken: GetSignupDipToken,
    private val vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
    private val dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    private val dipPrefs: DipPrefs,
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
    val selectedPlanProductId = mutableStateOf("")
    val dipSelectedCountry =
        mutableStateOf<DipCountriesResponse.DedicatedIpCountriesAvailable?>(null)
    private lateinit var userLocale: String

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

    fun getSupportedDipCountries() = viewModelScope.launch {
        getDipSupportedCountries.invoke().collect {
            it?.let {
                supportedDipCountriesList.value = it
                if (it.dedicatedIpCountriesAvailable.isEmpty().not()) {
                    selectDipCountry(it.dedicatedIpCountriesAvailable.first())
                }
            }
        }
    }

    fun getDipMonthlyPlan() = viewModelScope.launch {
        getDipMonthlyPlan.invoke().collect { monthlyPlan ->
            monthlyPlan?.let {
                dipMonthlyPlan.value = it
            }
        }
    }

    fun getDipYearlyPlan() = viewModelScope.launch {
        getDipYearlyPlan.invoke().collect { yearlyPlan ->
            yearlyPlan?.let {
                if (selectedPlanProductId.value.isEmpty()) {
                    selectedPlanProductId.value = it.id
                }
                dipYearlyPlan.value = it
            }
        }
    }

    fun hasActivePlaystoreSubscription() = viewModelScope.launch {
        vpnSubscriptionPaymentProvider.hasActiveSubscription().collect {
            hasAnActivePlaystoreSubscription.value = true
        }
    }

    fun showDedicatedIpSignupBanner() =
        dipPrefs.isDipSignupEnabled()

    fun selectDipCountry(selected: DipCountriesResponse.DedicatedIpCountriesAvailable) {
        dipSelectedCountry.value = selected
    }

    fun enableActivateTokenButton() {
        activateTokenButtonState.value = true
    }

    fun getSignupDipToken(): String =
        getSignupDipToken.invoke()

    fun purchaseSubscription(activity: Activity) {
        if (selectedPlanProductId.value.isEmpty()) {
            return
        }

        dipSubscriptionPaymentProvider.purchaseProduct(
            activity = activity,
            productId = selectedPlanProductId.value,
        ) { result ->
            result.fold(
                onSuccess = {
                    viewModelScope.launch {
                        validateDipSignup.invoke(dipPurchaseData = it).collect { result ->
                            result.fold(
                                onSuccess = {
                                    navigateToDedicatedIpLocationSelection()
                                },
                                onFailure = {
                                    TODO()
                                },
                            )
                        }
                    }
                },
                onFailure = {
                    TODO()
                },
            )
        }
    }

    private fun navigateToDedicatedIpLocationSelection() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.LocationSelection)
    }

    private fun navigateToDedicatedIpPurchaseSuccess() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupSuccess)
    }
}