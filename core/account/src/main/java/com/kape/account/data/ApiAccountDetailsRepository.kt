package com.kape.account.data

import com.kape.account.domain.AccountDetails
import com.kape.account.domain.AccountDetailsRepository
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [AccountDetailsRepository::class])
internal class ApiAccountDetailsRepository(
    private val api: AndroidAccountAPI,
) : AccountDetailsRepository {
    override suspend fun getAccountDetails(): AccountDetails? =
        suspendCancellableCoroutine { continuation ->
            api.accountDetails { details, _ ->
                continuation.resume(
                    details?.let {
                        AccountDetails(
                            username = it.username,
                            isExpired = it.expired,
                            daysRemaining = it.daysRemaining,
                            showExpiryWarning = it.expireAlert,
                        )
                    },
                )
            }
        }
}