package com.kape.profile.domain

import android.text.format.DateFormat
import com.kape.profile.data.NetworkDatasource
import com.kape.profile.data.NetworkDatasource.Companion.DATE_FORMAT
import com.kape.profile.models.Profile
import com.kape.profile.models.Subscription
import com.privateinternetaccess.account.AccountAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

class NetworkDatasourceImpl : NetworkDatasource, KoinComponent {
    private val api: AccountAPI by inject()

    override fun accountDetails(): Flow<Profile> = callbackFlow {
        api.accountDetails { details, error ->
            // TODO error handling
            if (details != null) {
                val subscription = Subscription(details.recurring, getExpirationDate(details.daysRemaining))
                Profile(details.username, subscription)
            }
        }
    }

    private fun getExpirationDate(daysRemaining: Int): String {
        val timestamp = Calendar.getInstance().timeInMillis + TimeUnit.DAYS.toMillis(daysRemaining.toLong())
        return DateFormat.format(DATE_FORMAT, timestamp).toString()
    }
}