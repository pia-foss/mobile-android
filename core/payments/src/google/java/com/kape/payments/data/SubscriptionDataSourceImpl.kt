package com.kape.payments.data

import com.kape.data.DI
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.prefs.SubscriptionPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

@Singleton([SubscriptionDataSource::class])
class SubscriptionDataSourceImpl(
    private val prefs: SubscriptionPrefs,
    private val api: AndroidAccountAPI,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : SubscriptionDataSource,
    KoinComponent {
    override suspend fun getAvailableVpnSubscriptions(): List<Subscription> =
        suspendCancellableCoroutine { cont ->
            api.vpnSubscriptions { details, error ->
                if (error.isNotEmpty() || details == null) {
                    cont.resume(emptyList())
                    return@vpnSubscriptions
                }
                val data = mutableListOf<Subscription>()
                for (item in details.availableProducts) {
                    data.add(Subscription(item.id, item.legacy, item.plan, item.price, null))
                }
                ioScope.launch {
                    prefs.storeVpnSubscriptions(data)
                    cont.resume(prefs.vpnSubscriptions.first { it.isNotEmpty() })
                }
            }
        }
}