package com.kape.payments.data

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.SubscriptionDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class SubscriptionDataSourceImpl(
    private val prefs: SubscriptionPrefs,
    private val api: AndroidAccountAPI,
) : SubscriptionDataSource, KoinComponent {

    override fun getAvailableVpnSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        api.vpnSubscriptions { details, error ->
            if (error.isNotEmpty() || details == null) {
                trySend(emptyList())
                return@vpnSubscriptions
            }
            val data = mutableListOf<Subscription>()
            for (item in details.availableProducts) {
                data.add(Subscription(item.id, item.legacy, item.plan, item.price, null))
            }
            prefs.storeVpnSubscriptions(data)
            trySend(prefs.getVpnSubscriptions())
        }
        awaitClose { channel.close() }
    }
}