package com.kape.payments.data

import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.models.Subscription
import com.kape.payments.utils.SubscriptionPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SubscriptionDataSourceImpl(private val prefs: SubscriptionPrefs) : SubscriptionDataSource, KoinComponent {

    private val api: AndroidAccountAPI by inject()

    override fun getAvailableSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        api.subscriptions { details, error ->
            if (error.isNotEmpty() || details == null) {
                trySend(emptyList())
                return@subscriptions
            }
            val data = mutableListOf<Subscription>()
            for (item in details.availableProducts) {
                data.add(Subscription(item.id, item.legacy, item.plan, item.price))
            }
            prefs.storeSubscriptions(data)
            trySend(prefs.getSubscriptions())
        }
        awaitClose { channel.close() }
    }
}