package com.kape.splash.data

import com.kape.splash.domain.LatestAppVersionDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.LatestClientVersion
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [LatestAppVersionDataSource::class])
class LatestAppVersionDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
) : LatestAppVersionDataSource {
    override suspend fun getLatestAppVersion(): LatestClientVersion? =
        suspendCancellableCoroutine { cont ->
            accountApi.latestRelease { latestRelease, errors ->
                if (errors.isNotEmpty()) {
                    cont.resume(null)
                    return@latestRelease
                }
                cont.resume(latestRelease)
            }
        }
}