package com.kape.dedicatedip.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.utils.server.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class DipViewModel(
    private val regionRepository: VpnRegionRepository,
    private val activateDipUseCase: ActivateDipUseCase,
    private val dipPrefs: DipPrefs,
    private val router: Router,
) : ViewModel(), KoinComponent {

    val dipList = mutableStateListOf<VpnServer>()
    val activationState = mutableStateOf<DipApiResult?>(null)
    private lateinit var userLocale: String

    fun navigateBack() {
        router.handleFlow(ExitFlow.DedicatedIp)
    }

    fun loadDedicatedIps(locale: String) = viewModelScope.launch {
        userLocale = locale
        dipList.clear()
        for (dip in dipPrefs.getDedicatedIps()) {
            regionRepository.fetchRegions(userLocale).collect {
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
}