package com.kape.utils

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AppInfo
import com.kape.contracts.GetAppLatestVersion
import com.kape.data.DI
import com.kape.data.VERSION_DIFFERENCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

class UpdateAvailableManager(
    private val buildConfigProvider: BuildConfigProvider,
    private val getAppLatestVersionUseCase: GetAppLatestVersion,
    @Named(DI.UPDATE_URL) private val appUpdateUrl: String,
    private val appInfo: AppInfo,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) {
    private var updateUrl: String = ""
    private val _hasUpdateAvailable = MutableStateFlow(false)
    val hasUpdateAvailable = _hasUpdateAvailable.asStateFlow()

    init {
        ioScope.launch {
            val latestAppVersion = getAppLatestVersionUseCase.invoke()
            latestAppVersion?.let { appVersion ->
                updateUrl = latestAppVersion.url
                if (buildConfigProvider.isGoogleFlavor()) {
                    _hasUpdateAvailable.update { appInfo.versionCode < appVersion.versionCode.toInt() - VERSION_DIFFERENCE }
                } else {
                    _hasUpdateAvailable.update { appInfo.versionCode < appVersion.versionCode.toInt() }
                }
            }
        }
    }

    fun getDownloadUrl(): String =
        when {
            buildConfigProvider.isGoogleFlavor() -> appUpdateUrl
            buildConfigProvider.isAmazonFlavor() -> appUpdateUrl
            else -> updateUrl
        }
}