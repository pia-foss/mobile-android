package com.kape.sidemenu.ui.vm

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.router.About
import com.kape.router.Connection
import com.kape.router.DedicatedIpActivateToken
import com.kape.router.PerAppSettings
import com.kape.router.Profile
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.Splash
import com.kape.router.WebDestination
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    val versionCode: Int,
    val versionName: String,
    val router: Router,
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
            router.updateDestination(Splash)
        }
    }

    fun navigateToVpnConnect() {
        router.updateDestination(Connection)
    }

    fun navigateToProfile() {
        router.updateDestination(Profile)
    }

    fun navigateToSettings() {
        router.updateDestination(Settings)
    }

    fun navigateToPerAppSettings() {
        router.updateDestination(PerAppSettings)
    }

    fun navigateToDedicatedIp() {
        router.updateDestination(DedicatedIpActivateToken)
    }

    fun navigateToPrivacyPolicy() {
        router.updateDestination(WebDestination.Privacy)
    }

    fun navigateToSupport() {
        router.updateDestination(WebDestination.Support)
    }

    fun navigateToAbout() {
        router.updateDestination(About)
    }
}