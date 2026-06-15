package com.kape.splash.domain

import com.kape.contracts.GetAppLatestVersion
import com.privateinternetaccess.account.model.response.LatestClientVersion
import org.koin.core.annotation.Singleton

@Singleton(binds = [GetAppLatestVersion::class])
class GetAppLatestVersionUseCase(
    private val dataSource: LatestAppVersionDataSource,
) : GetAppLatestVersion {
    override suspend fun invoke(): LatestClientVersion? = dataSource.getLatestAppVersion()
}