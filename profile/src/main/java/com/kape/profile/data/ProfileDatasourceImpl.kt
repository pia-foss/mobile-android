package com.kape.profile.data

import android.text.format.DateFormat
import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.models.Profile
import com.kape.profile.models.Subscription
import com.kape.profile.models.Subscription.Companion.DATE_FORMAT
import com.privateinternetaccess.account.AccountAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration


class ProfileDatasourceImpl : ProfileDatasource, KoinComponent {
    private val api: AccountAPI by inject()

    override fun accountDetails(): Flow<Profile> = callbackFlow {
        api.accountDetails { details, _ ->
            if (details != null) {
                val subscription = Subscription(details.recurring, getExpirationDate(details.daysRemaining))
                Profile(details.username, subscription)
            }
        }
    }

    private fun getExpirationDate(daysRemaining: Int): String {
        val timestamp = System.currentTimeMillis() + Duration.ofDays(daysRemaining.toLong()).toMillis()
        return DateFormat.format(DATE_FORMAT, timestamp).toString()
    }
}