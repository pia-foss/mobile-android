package com.kape.login.data

import com.kape.core.ApiResult
import com.kape.core.getApiError
import com.kape.login.domain.AuthenticationDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthenticationDataSourceImpl : AuthenticationDataSource, KoinComponent {

    private val api: AndroidAccountAPI by inject()

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
}