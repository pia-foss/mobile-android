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
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.payments.ui.PaymentProvider
import com.kape.payments.utils.PurchaseHistoryState
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class DipViewModel(
    private val regionRepository: VpnRegionRepository,
    private val activateDipUseCase: ActivateDipUseCase,
    private val getDipSupportedCountries: GetDipSupportedCountries,
    private val getDipMonthlyPlan: GetDipMonthlyPlan,
    private val getDipYearlyPlan: GetDipYearlyPlan,
    private val paymentProvider: PaymentProvider,
    private val dipPrefs: DipPrefs,
    private val router: Router,
) : ViewModel(), KoinComponent {

    val dipList = mutableStateListOf<VpnServer>()
    val activationState = mutableStateOf<DipApiResult?>(null)
    val hasAnActivePlaystoreSubscription = mutableStateOf(false)
    val supportedDipCountriesList = mutableStateOf<SupportedCountries?>(null)
    val dipMonthlyPlan = mutableStateOf<DedicatedIpMonthlyPlan?>(null)
    val dipYearlyPlan = mutableStateOf<DedicatedIpYearlyPlan?>(null)
    private lateinit var userLocale: String

    fun navigateBack() {
        router.handleFlow(ExitFlow.DedicatedIp)
    }

    fun navigateToDedicatedIpPlans() {
        router.handleFlow(EnterFlow.DedicatedIpPlans)
    }

    fun loadDedicatedIps(locale: String) = viewModelScope.launch {
        userLocale = locale
        dipList.clear()
        for (dip in dipPrefs.getDedicatedIps()) {
            regionRepository.fetchVpnRegions(userLocale).collect {
                dipList.addAll(it.filter { it.isDedicatedIp })
            }
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

    fun getActivePlaystoreSubscription() {
        viewModelScope.launch {
            paymentProvider.purchaseHistoryState.collect {
                when (it) {
                    is PurchaseHistoryState.PurchaseHistorySuccess -> {
                        hasAnActivePlaystoreSubscription.value = true
                    }
                    PurchaseHistoryState.Default -> {
                        // no=op
                    }
                    PurchaseHistoryState.PurchaseHistoryFailed -> {
                        hasAnActivePlaystoreSubscription.value = false
                    }
                }
            }
        }
        paymentProvider.getPurchaseHistory()
    }

    fun showDedicatedIpSignupBanner() =
        dipPrefs.isDipSignupEnabled() && dipPrefs.showDedicatedIpHomeBanner()
}