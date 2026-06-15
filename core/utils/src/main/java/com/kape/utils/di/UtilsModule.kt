package com.kape.utils.di

import android.content.Context
import androidx.work.WorkManager
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.AppInfo
import com.kape.contracts.GetAppLatestVersion
import com.kape.data.DI
import com.kape.utils.PlatformUtils
import com.kape.utils.UpdateAvailableManager
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class UtilsModule {
    @Singleton
    fun providePlatformUtils(context: Context): PlatformUtils = PlatformUtils(context)

    @Singleton
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)

    @Singleton
    fun provideUpdateAvailableManager(
        buildConfigProvider: BuildConfigProvider,
        getAppLatestVersionUseCase: GetAppLatestVersion,
        @Named(DI.UPDATE_URL) appUpdateUrl: String,
        appInfo: AppInfo,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): UpdateAvailableManager =
        UpdateAvailableManager(
            buildConfigProvider,
            getAppLatestVersionUseCase,
            appUpdateUrl,
            appInfo,
            ioScope,
        )
}