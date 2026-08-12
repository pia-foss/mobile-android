package com.kape.sidemenu.ui.vm

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.AppInfo
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.About
import com.kape.data.Connection
import com.kape.data.DI
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.PerAppSettings
import com.kape.data.Profile
import com.kape.data.Settings
import com.kape.data.Splash
import com.kape.data.WebDestination
import com.kape.profile.domain.GetProfileUseCase
import com.kape.utils.UpdateAvailableManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class SideMenuViewModel(
    private val profileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val appInfo: AppInfo,
    private val router: Router,
    private val updateAvailableManager: UpdateAvailableManager,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val username = mutableStateOf("")
    val showExpire = mutableStateOf(false)
    val daysRemaining = mutableIntStateOf(0)
    val updateAvailable = updateAvailableManager.hasUpdateAvailable

    init {
        viewModelScope.launch(ioDispatcher) {
            val it = profileUseCase.getProfile()
            it?.let {
                username.value = it.username.uppercase()
                showExpire.value = it.subscription.showExpire
                daysRemaining.value = it.subscription.daysRemaining
            }
        }
    }

    fun logout() =
        viewModelScope.launch(ioDispatcher) {
            logoutUseCase.logout()
            router.updateDestination(Splash)
        }

    fun getDownloadLink(): String = updateAvailableManager.getDownloadUrl()

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