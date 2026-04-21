package com.kape.login.data

import com.kape.contracts.AuthenticationDataSource
import com.kape.data.auth.ApiResult
import com.kape.data.auth.getApiError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

private const val STORE = "google_play"

@Singleton(binds = [AuthenticationDataSource::class])
class AuthenticationDataSourceImpl(
    private val api: AndroidAccountAPI,
) : AuthenticationDataSource {
    override fun isUserLoggedIn(): Boolean = !api.apiToken().isNullOrEmpty() && !api.vpnToken().isNullOrEmpty()

    override suspend fun login(
        username: String,
        password: String,
    ): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.loginWithCredentials(username, password) {
                if (it.isNotEmpty()) {
                    cont.resume(ApiResult.Error(getApiError(it.last().code)))
                    return@loginWithCredentials
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun logout(): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.logout {
                if (it.isNotEmpty()) {
                    cont.resume(ApiResult.Error(getApiError(it.last().code)))
                    return@logout
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun loginWithEmail(email: String): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.loginLink(email) {
                if (it.isNotEmpty()) {
                    cont.resume(ApiResult.Error(getApiError(it.last().code)))
                    return@loginLink
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.loginWithReceipt(STORE, receiptToken, productId, packageName) {
                if (it.isNotEmpty()) {
                    cont.resume(ApiResult.Error(getApiError(it.last().code)))
                    return@loginWithReceipt
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun migrateToken(apiToken: String): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.migrateApiToken(apiToken) {
                if (it.isNotEmpty()) {
                    cont.resume(ApiResult.Error(getApiError(it.last().code)))
                    return@migrateApiToken
                }
                cont.resume(ApiResult.Success)
            }
        }
}