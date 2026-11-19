package com.kape.settings.domain

import android.content.pm.ApplicationInfo

interface InstalledAppsSource {
    suspend fun load(): List<ApplicationInfo>

    fun label(app: ApplicationInfo): String
}