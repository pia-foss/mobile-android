package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.profile.data.models.Profile
import com.kape.profile.domain.DeleteAccountUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.profile.ui.screens.mobile.IDLE
import com.kape.profile.ui.screens.mobile.LOADING
import com.kape.profile.ui.screens.mobile.ProfileScreenState
import com.kape.profile.ui.screens.mobile.createSuccessState
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ProfileViewModel(
    private val useCase: GetProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val router: Router,
) :
    ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(IDLE)
    val screenState: StateFlow<ProfileScreenState> = _state

    init {
        loadProfile()
    }

    fun navigateBack() {
        router.handleFlow(Back)
    }

    fun navigateToLogin() {
        router.handleFlow(EnterFlow.Login)
    }

    fun navigateToSignUp() {
        router.handleFlow(EnterFlow.Splash)
    }

    fun exitApp() {
        router.handleFlow(Exit)
    }

    fun deleteAccount() = viewModelScope.launch {
        _state.emit(LOADING)
        deleteAccountUseCase.deleteAccount().collect {
            logoutUseCase.logout().collect {
                router.handleFlow(EnterFlow.AccountDeleted)
            }
        }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase.logout().collect {
            router.handleFlow(EnterFlow.Splash)
        }
    }

    private fun loadProfile() = viewModelScope.launch {
        _state.emit(LOADING)
        useCase.getProfile().collect { profile ->
            if (profile == null) {
                _state.emit(IDLE)
            } else {
                _state.emit(getState(profile))
            }
        }
    }

    private fun getState(profile: Profile): ProfileScreenState {
        return createSuccessState(
            profile.username,
            profile.subscription.expirationDate,
            profile.subscription.isExpired,
        )
    }
}