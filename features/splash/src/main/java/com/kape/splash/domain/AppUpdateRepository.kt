package com.kape.splash.domain

interface AppUpdateRepository {
    suspend fun getLatestRelease(): AppRelease?
}

data class AppRelease(
    val url: String,
    val versionCode: Int,
)