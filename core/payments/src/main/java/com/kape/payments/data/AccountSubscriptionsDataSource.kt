package com.kape.payments.data

import com.kape.data.model.Subscription
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

internal fun interface SubscriptionsDataSource {
    suspend fun fetchSubscriptions(): List<Subscription>?
}

@Singleton(binds = [SubscriptionsDataSource::class])
internal class AccountSubscriptionsDataSource(
    private val api: AndroidAccountAPI,
) : SubscriptionsDataSource {
    override suspend fun fetchSubscriptions(): List<Subscription>? =
        suspendCancellableCoroutine { continuation ->
            api.vpnSubscriptions { details, errors ->
                continuation.resume(
                    if (errors.isNotEmpty() || details == null) {
                        null
                    } else {
                        details.availableProducts.map { Subscription(it.id, it.legacy, it.plan, it.price, null) }
                    },
                )
            }
        }
}