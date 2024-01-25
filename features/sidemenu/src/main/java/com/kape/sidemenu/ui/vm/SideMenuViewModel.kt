package com.kape.sidemenu.ui.vm

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.LogoutUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.router.EnterFlow
import com.kape.router.Router
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    val versionCode: Int,
    val versionName: String,
    private val router: Router,
) : ViewModel(), KoinComponent {

    val username = mutableStateOf("")
    val showExpire = mutableStateOf(false)
    val daysRemaining = mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            profileUseCase.getProfile().collect {
                it?.let {
                    username.value = it.username.uppercase()
                    showExpire.value = it.subscription.showExpire
                    daysRemaining.value = it.subscription.daysRemaining
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase.logout().collect {
            router.handleFlow(EnterFlow.Splash)
        }
    }

    fun navigateToProfile() {
        router.handleFlow(EnterFlow.Profile)
    }

    fun navigateToSettings() {
        router.handleFlow(EnterFlow.Settings)
    }

    fun navigateToPerAppSettings() {
        router.handleFlow(EnterFlow.PerAppSettings)
    }

    fun navigateToDedicatedIp() {
        router.handleFlow(EnterFlow.DedicatedIp)
    }

    fun navigateToPrivacyPolicy() {
        router.handleFlow(EnterFlow.PrivacyPolicy)
    }

    fun navigateToSupport() {
        router.handleFlow(EnterFlow.Support)
    }

    fun navigateToAbout() {
        router.handleFlow(EnterFlow.About)
    }
}