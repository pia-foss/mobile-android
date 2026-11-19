package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.account.domain.usecase.GetAccountDetailsUseCase
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.Splash
import com.kape.data.WebDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ProfileState(
    val isLoading: Boolean = true,
    val username: String = "",
    val expiresAtMillis: Long? = null,
    val isExpired: Boolean = false,
)

@KoinViewModel
class ProfileViewModel(
    private val getAccountDetails: GetAccountDetailsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val router: Router,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val account = getAccountDetails()
            _state.value =
                ProfileState(
                    isLoading = false,
                    username = account?.username.orEmpty(),
                    expiresAtMillis = account?.expiresAtMillis(System.currentTimeMillis()),
                    isExpired = account?.isExpired ?: false,
                )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.logout()
            router.updateDestination(Splash)
        }
    }

    fun navigateToDeleteAccount() = router.updateDestination(WebDestination.DeleteAccount)
}