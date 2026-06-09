package com.kape.splash.domain

import com.privateinternetaccess.account.model.response.LatestClientVersion
import org.koin.core.annotation.Singleton

@Singleton
class GetAppLatestVersionUseCase(
    private val dataSource: LatestAppVersionDataSource,
) {
    suspend fun getLatestVersion(): LatestClientVersion? = dataSource.getLatestAppVersion()
}