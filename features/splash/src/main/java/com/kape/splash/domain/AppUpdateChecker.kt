package com.kape.splash.domain

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AppInfo
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.di.Qualifiers
import com.kape.data.VERSION_DIFFERENCE
import com.kape.splash.domain.usecase.GetUpdateUrlUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton(binds = [AppUpdateStatus::class])
internal class AppUpdateChecker(
    repository: AppUpdateRepository,
    appInfo: AppInfo,
    buildConfig: BuildConfigProvider,
    private val getUpdateUrl: GetUpdateUrlUseCase,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
) : AppUpdateStatus {
    override val hasUpdateAvailable: StateFlow<Boolean> =
        flow {
            val latest = repository.getLatestRelease()
            emit(latest != null && isUpdateAvailable(appInfo.versionCode, latest.versionCode, buildConfig.isGoogleFlavor()))
        }.stateIn(appScope, SharingStarted.Lazily, false)

    override suspend fun getDownloadUrl(): String? = getUpdateUrl()
}

/**
 * Google Play builds number their versions [VERSION_DIFFERENCE] above the published release
 * they correspond to.
 */
internal fun isUpdateAvailable(
    installedVersionCode: Int,
    latestVersionCode: Int,
    isGooglePlayBuild: Boolean,
): Boolean {
    val comparable = if (isGooglePlayBuild) latestVersionCode - VERSION_DIFFERENCE else latestVersionCode
    return installedVersionCode < comparable
}