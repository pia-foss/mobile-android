package com.kape.utils.di

import android.content.Context
import androidx.work.WorkManager
import com.kape.utils.PlatformUtils
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class UtilsModule {
    @Singleton
    fun providePlatformUtils(context: Context): PlatformUtils = PlatformUtils(context)

    @Singleton
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)
}