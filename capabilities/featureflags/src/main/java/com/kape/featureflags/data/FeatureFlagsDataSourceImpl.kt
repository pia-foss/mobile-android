package com.kape.featureflags.data

import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([FeatureFlagsDataSource::class])
class FeatureFlagsDataSourceImpl(
    private val api: AndroidAccountAPI,
) : FeatureFlagsDataSource {
    override suspend fun invoke(): List<String> =
        suspendCancellableCoroutine { continuation ->
            api.featureFlags { details, error ->
                details?.let {
                    continuation.resume(it.flags)
                } ?: run {
                    continuation.resume(emptyList())
                }
            }
        }
}