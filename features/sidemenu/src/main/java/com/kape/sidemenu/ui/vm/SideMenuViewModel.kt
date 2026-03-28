package com.kape.sidemenu.ui.vm

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.AppInfo
import com.kape.contracts.Router
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.profile.domain.GetProfileUseCase
import com.kape.contracts.data.About
import com.kape.contracts.data.Connection
import com.kape.contracts.data.DedicatedIpActivateToken
import com.kape.contracts.data.PerAppSettings
import com.kape.contracts.data.Profile
import com.kape.contracts.data.Settings
import com.kape.contracts.data.Splash
import com.kape.contracts.data.WebDestination
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val appInfo: AppInfo,
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

    fun getVersionName(): String = appInfo.versionName

    fun getVersionCode(): String = "${appInfo.versionCode}"
}