package com.kape.settings.data

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import com.kape.contracts.di.Qualifiers
import com.kape.settings.domain.InstalledAppsSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton(binds = [InstalledAppsSource::class])
class InstalledApps(
    context: Context,
    @Named(Qualifiers.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : InstalledAppsSource {
    private val packageManager = context.packageManager

    override suspend fun load(): List<ApplicationInfo> =
        withContext(ioDispatcher) {
            packageManager
                .getPackagesHoldingPermissions(arrayOf(Manifest.permission.INTERNET), 0)
                .mapNotNull { it.applicationInfo }
                .sortedBy { label(it).lowercase() }
        }

    override fun label(app: ApplicationInfo): String = packageManager.getApplicationLabel(app).toString()
}