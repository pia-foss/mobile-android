package com.kape.splash.data

import com.kape.splash.domain.AppRelease
import com.kape.splash.domain.AppUpdateRepository
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [AppUpdateRepository::class])
internal class AccountAppUpdateRepository(
    private val api: AndroidAccountAPI,
) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppRelease? =
        suspendCancellableCoroutine { continuation ->
            api.latestRelease { latestRelease, errors ->
                val release =
                    latestRelease
                        ?.takeIf { errors.isEmpty() && it.url.isNotEmpty() }
                        ?.let { release -> release.versionCode.toIntOrNull()?.let { AppRelease(release.url, it) } }
                continuation.resume(release)
            }
        }
}