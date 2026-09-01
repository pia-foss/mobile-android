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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import kotlin.time.Duration.Companion.milliseconds

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
        refreshProfile()
    }

    // getProfile() can transiently return null (network blip, encrypted token store not yet
    // warmed up after process start) even when the user is genuinely logged in. Retry a few
    // times so a transient failure doesn't leave the header blank for the rest of the session.
    // Safe to call repeatedly (e.g. every time the drawer opens): it only ever overwrites the
    // displayed values on success, never clears them on failure.
    fun refreshProfile() {
        if (username.value.isNotEmpty()) return
        viewModelScope.launch(ioDispatcher) {
            repeat(PROFILE_FETCH_ATTEMPTS) { attempt ->
                val profile = profileUseCase.getProfile()
                if (profile != null) {
                    username.value = profile.username.uppercase()
                    showExpire.value = profile.subscription.showExpire
                    daysRemaining.value = profile.subscription.daysRemaining
                    return@launch
                }
                if (attempt < PROFILE_FETCH_ATTEMPTS - 1) delay(PROFILE_FETCH_RETRY_DELAY_MS.milliseconds)
            }
        }
    }

    fun logout() =
        viewModelScope.launch(ioDispatcher) {
            logoutUseCase.logout()
            username.value = ""
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

    companion object {
        private const val PROFILE_FETCH_ATTEMPTS = 5
        private const val PROFILE_FETCH_RETRY_DELAY_MS = 300L
    }
}