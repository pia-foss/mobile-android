package com.kape.signup.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.account.domain.usecase.LoginWithPurchaseHistoryUseCase
import com.kape.account.domain.usecase.ReceiptLoginProgress
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.data.TvLoginUsername
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class WelcomeBackViewModel(
    private val router: Router,
    private val loginWithPurchaseHistory: LoginWithPurchaseHistoryUseCase,
    private val platformUtils: PlatformUtils,
) : ViewModel() {
    private val _state = MutableStateFlow<ReceiptLoginProgress>(ReceiptLoginProgress.Idle)
    val state: StateFlow<ReceiptLoginProgress> = _state.asStateFlow()
    private var receiptLoginJob: Job? = null

    fun onUsernameAndPasswordClicked() = router.updateDestination(if (platformUtils.isTv()) TvLoginUsername else LoginWithCredentials)

    fun onPlayStoreAccountClicked(packageName: String) {
        receiptLoginJob?.cancel()
        receiptLoginJob =
            viewModelScope.launch {
                loginWithPurchaseHistory(packageName).collect { progress ->
                    if (progress is ReceiptLoginProgress.Succeeded) {
                        router.updateDestination(progress.destination)
                    } else {
                        _state.value = progress
                    }
                }
            }
    }
}