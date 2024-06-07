package com.kape.dedicatedip.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.dedicatedip.data.models.DedicatedIpMonthlyPlan
import com.kape.dedicatedip.data.models.DedicatedIpYearlyPlan
import com.kape.dedicatedip.data.models.SupportedCountries
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.GetSignupDipToken
import com.kape.dedicatedip.domain.ValidateDipSignup
import com.kape.dedicatedip.utils.DedicatedIpStep
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.payments.ui.PaymentProvider
import com.kape.router.Back
import com.kape.router.Router
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.RegionListProvider
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
    private val paymentProvider: PaymentProvider,
    private val dipPrefs: DipPrefs,
    private val router: Router,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<DedicatedIpStep?>(null)
    val state: StateFlow<DedicatedIpStep?> = _state
    val activateTokenButtonState = mutableStateOf(false)

    val dipList = mutableStateListOf<VpnServer>()
    val activationState = mutableStateOf<DipApiResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(true)
    val supportedDipCountriesList = mutableStateOf<SupportedCountries?>(null)
    val dipMonthlyPlan = mutableStateOf<DedicatedIpMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DedicatedIpYearlyPlan?>(null)
    val dipSelectedCountry =
        mutableStateOf<SupportedCountries.DedicatedIpCountriesAvailable?>(null)
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

    fun navigateToDedicatedIpLocationSelection() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.LocationSelection)
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
            supportedDipCountriesList.value = it
            selectDipCountry(it.dedicatedIpCountriesAvailable[0])
        }
    }

    fun getDipMonthlyPlan() = viewModelScope.launch {
        getDipMonthlyPlan.invoke().collect {
            dipMonthlyPlan.value = it
        }
    }

    fun getDipYearlyPlan() = viewModelScope.launch {
        getDipYearlyPlan.invoke().collect {
            dipYearlyPlan.value = it
        }
    }

    fun hasActivePlaystoreSubscription() = viewModelScope.launch {
        paymentProvider.hasActiveSubscription().collect {
            hasAnActivePlaystoreSubscription.value = it
        }
    }

    fun showDedicatedIpSignupBanner() =
        dipPrefs.isDipSignupEnabled()

    fun selectDipCountry(selected: SupportedCountries.DedicatedIpCountriesAvailable?) {
        dipSelectedCountry.value = selected
    }

    fun enableActivateTokenButton() {
        activateTokenButtonState.value = true
    }

    fun getSignupDipToken(): String =
        getSignupDipToken.invoke()

    fun signup() = viewModelScope.launch {
        validateDipSignup.signup().collect { result ->
            result.fold(
                onSuccess = {
                    navigateToDedicatedIpPurchaseSuccess()
                },
                onFailure = {
                    TODO()
                },
            )
        }
    }

    private fun navigateToDedicatedIpPurchaseSuccess() = viewModelScope.launch {
        _state.emit(DedicatedIpStep.SignupSuccess)
    }
}