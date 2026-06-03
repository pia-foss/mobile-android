package com.kape.payments.data

import com.kape.data.DI
import com.kape.payments.domain.SubscriptionDataSource
import com.kape.payments.prefs.SubscriptionPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton(binds = [SubscriptionDataSource::class])
class SubscriptionDataSourceImpl(
    private val prefs: SubscriptionPrefs,
    private val api: AndroidAccountAPI,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : SubscriptionDataSource,
    KoinComponent {
    override suspend fun getAvailableVpnSubscriptions(): List<Subscription> = emptyList()
}