package com.kape.regions.common.api

import android.app.Application
import android.content.Context
import com.privateinternetaccess.regions.PlatformInstancesProvider
import org.koin.core.annotation.Singleton

@Singleton(binds = [PlatformInstancesProvider::class])
internal class AndroidPlatformInstancesProvider(
    private val context: Context,
) : PlatformInstancesProvider {
    override fun provideApplicationContext(): Application = context.applicationContext as Application
}