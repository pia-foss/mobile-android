package com.kape.featureflags.data

import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class FeatureFlagsDataSourceImpl(private val api: AndroidAccountAPI) :
    FeatureFlagsDataSource,
    KoinComponent {

    override fun invoke(): Flow<List<String>> = callbackFlow {
        api.featureFlags { details, error ->
            details?.let {
                trySend(it.flags)
            } ?: run {
                trySend(emptyList())
            }
        }
        awaitClose { channel.close() }
    }
}