package com.kape.sidemenu.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.account.domain.usecase.GetAccountDetailsUseCase
import com.kape.contracts.AppInfo
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.Router
import com.kape.data.About
import com.kape.data.Connection
import com.kape.data.DedicatedIpActivateToken
import com.kape.data.PerAppSettings
import com.kape.data.Profile
import com.kape.data.Settings
import com.kape.data.Splash
import com.kape.data.WebDestination
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SideMenuViewModel(
    private val getAccountDetails: GetAccountDetailsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val router: Router,
    private val appUpdateStatus: AppUpdateStatus,
    appInfo: AppInfo,
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    val hasUpdateAvailable: StateFlow<Boolean> = appUpdateStatus.hasUpdateAvailable
    val versionName: String = appInfo.versionName
    val versionCode: String = appInfo.versionCode.toString()

    private var loadingUsername: Job? = null

    init {
        refreshUsername()
    }

    fun refreshUsername() {
        if (_username.value.isNotEmpty() || loadingUsername?.isActive == true) return
        loadingUsername =
            viewModelScope.launch {
                getAccountDetails()?.let { _username.value = it.username.uppercase() }
            }
    }

    suspend fun getDownloadUrl(): String? = appUpdateStatus.getDownloadUrl()

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.logout()
            _username.value = ""
            router.updateDestination(Splash)
        }
    }

    fun navigateToVpnConnect() = router.updateDestination(Connection)

    fun navigateToProfile() = router.updateDestination(Profile)

    fun navigateToSettings() = router.updateDestination(Settings)

    fun navigateToPerAppSettings() = router.updateDestination(PerAppSettings)

    fun navigateToDedicatedIp() = router.updateDestination(DedicatedIpActivateToken)

    fun navigateToPrivacyPolicy() = router.updateDestination(WebDestination.Privacy)

    fun navigateToSupport() = router.updateDestination(WebDestination.Support)

    fun navigateToAbout() = router.updateDestination(About)
}