package com.kape.profile.data

import com.kape.profile.domain.ProfileDatasource
import com.kape.profile.models.Profile
import com.kape.profile.models.Subscription
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class ProfileDatasourceImpl : ProfileDatasource, KoinComponent {
    private val api: AndroidAccountAPI by inject()

    override fun accountDetails(): Flow<Profile?> = callbackFlow {
        api.accountDetails { details, errorList ->
            if (details != null) {
                val subscription = Subscription(details.expired, details.daysRemaining, details.expireAlert)
                trySend(Profile(details.username, subscription))
            } else {
                trySend(null)
            }
        }
        awaitClose { channel.close() }
    }


}
