package com.kape.payments.data

import com.kape.payments.SubscriptionPrefs
import com.kape.payments.domain.SubscriptionDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton(binds = [SubscriptionDataSource::class])
class SubscriptionDataSourceImpl(
    private val prefs: SubscriptionPrefs,
    private val api: AndroidAccountAPI,
) : SubscriptionDataSource, KoinComponent {

    override suspend fun getAvailableVpnSubscriptions(): List<Subscription> = emptyList()
}