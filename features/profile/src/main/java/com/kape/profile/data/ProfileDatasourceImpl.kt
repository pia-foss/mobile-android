package com.kape.profile.data

import com.kape.profile.data.models.Profile
import com.kape.profile.data.models.Subscription
import com.kape.profile.domain.ProfileDatasource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class ProfileDatasourceImpl(private val api: AndroidAccountAPI) : ProfileDatasource, KoinComponent {

    override fun accountDetails(): Flow<Profile?> = callbackFlow {
        api.accountDetails { details, errorList ->
            if (details != null) {
                val subscription =
                    Subscription(details.expired, details.daysRemaining, details.expireAlert)
                trySend(Profile(details.username, subscription))
            } else {
                trySend(null)
            }
        }
        awaitClose { channel.close() }
    }
}