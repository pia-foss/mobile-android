package com.kape.login.data

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.data.auth.ApiResult
import com.kape.contracts.data.auth.getApiError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

private const val STORE = "google_play"

@Singleton(binds = [AuthenticationDataSource::class])
class AuthenticationDataSourceImpl(private val api: AndroidAccountAPI) :
    AuthenticationDataSource {

    override fun isUserLoggedIn(): Boolean {
        return !api.apiToken().isNullOrEmpty() && !api.vpnToken().isNullOrEmpty()
    }

    override fun login(username: String, password: String): Flow<ApiResult> = callbackFlow {
        api.loginWithCredentials(username, password) {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@loginWithCredentials
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    override fun logout(): Flow<ApiResult> = callbackFlow {
        api.logout {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@logout
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    override fun loginWithEmail(email: String): Flow<ApiResult> = callbackFlow {
        api.loginLink(email) {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@loginLink
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    override fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): Flow<ApiResult> = callbackFlow {
        api.loginWithReceipt(STORE, receiptToken, productId, packageName) {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@loginWithReceipt
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    override fun migrateToken(apiToken: String): Flow<ApiResult> = callbackFlow {
        api.migrateApiToken(apiToken) {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@migrateApiToken
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }
}