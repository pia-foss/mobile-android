package com.kape.payments.data

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.SubscriptionDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

@Singleton([SubscriptionDataSource::class])
class SubscriptionDataSourceImpl(
    private val prefs: SubscriptionPrefs,
    private val api: AndroidAccountAPI,
) : SubscriptionDataSource,
    KoinComponent {
    override suspend fun getAvailableVpnSubscriptions(): List<Subscription> =
        suspendCancellableCoroutine { cont ->
            api.amazonSubscriptions { details, error ->
                if (error.isNotEmpty() || details == null) {
                    cont.resume(emptyList())
                    return@amazonSubscriptions
                }
                val data = mutableListOf<Subscription>()
                for (item in details.availableProducts) {
                    data.add(Subscription(item.id, item.legacy, item.plan, item.price))
                }
                prefs.storeVpnSubscriptions(data)
                cont.resume(prefs.getVpnSubscriptions())
            }
        }
}