package com.kape.login.data

import com.kape.login.utils.ApiResult
import com.kape.login.utils.Prefs
import com.kape.login.utils.getApiError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LoginRepository(private val api: AndroidAccountAPI, private val prefs: Prefs) {

    fun isUserLoggedIn() = prefs.isUserLoggedIn()

    fun login(username: String, password: String): Flow<ApiResult> = callbackFlow {
        api.loginWithCredentials(username, password) {
            if (it.isNotEmpty()) {
                prefs.setUserLoggedIn(false)
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@loginWithCredentials
            }
            prefs.setUserLoggedIn(true)
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    fun logout(): Flow<ApiResult> = callbackFlow {
        api.logout {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@logout
            }
            prefs.setUserLoggedIn(false)
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }
}