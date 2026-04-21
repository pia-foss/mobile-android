package com.kape.profile.data

import com.kape.profile.data.models.Profile
import com.kape.profile.data.models.Subscription
import com.kape.profile.domain.ProfileDatasource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([ProfileDatasource::class])
class ProfileDatasourceImpl(
    private val api: AndroidAccountAPI,
) : ProfileDatasource {
    override suspend fun accountDetails(): Profile? =
        suspendCancellableCoroutine { cont ->
            api.accountDetails { details, errorList ->
                if (details != null) {
                    val subscription =
                        Subscription(details.expired, details.daysRemaining, details.expireAlert)
                    cont.resume(Profile(details.username, subscription))
                } else {
                    cont.resume(null)
                }
            }
        }

    override suspend fun deleteAccount(): Boolean =
        suspendCancellableCoroutine { cont ->
            api.deleteAccount {
                cont.resume(it.isEmpty())
            }
        }
}