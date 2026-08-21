package com.kape.featureflags.data

import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.kape.localprefs.prefs.FeaturePrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([FeatureFlagsDataSource::class])
class FeatureFlagsDataSourceImpl(
    private val api: AndroidAccountAPI,
    private val featurePrefs: FeaturePrefs,
) : FeatureFlagsDataSource {
    override suspend fun invoke(): List<String> =
        suspendCancellableCoroutine { continuation ->
            api.featureFlags { details, error ->
                if (error.isNotEmpty()) {
                    continuation.resume(emptyList())
                    return@featureFlags
                }
                CoroutineScope(continuation.context).launch {
                    featurePrefs.setFlags(details?.flags ?: emptyList())
                    continuation.resume(details?.flags ?: emptyList())
                }
            }
        }
}