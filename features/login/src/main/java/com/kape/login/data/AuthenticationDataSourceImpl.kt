package com.kape.login.data

import com.kape.contracts.AuthenticationDataSource
import com.kape.data.auth.ApiResult
import com.kape.data.auth.getApiError
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

private const val STORE = "google_play"

private fun List<AccountRequestError>.toApiError(): ApiResult.Error {
    val lastError = last()
    return ApiResult.Error(getApiError(lastError.code), lastError.code, lastError.message)
}

@Singleton(binds = [AuthenticationDataSource::class])
class AuthenticationDataSourceImpl(
    private val api: AndroidAccountAPI,
) : AuthenticationDataSource {
    override suspend fun isUserLoggedIn(): Boolean {
        repeat(LOGIN_CHECK_ATTEMPTS) {
            if (hasTokens()) return true
            delay(LOGIN_CHECK_RETRY_DELAY_MS.milliseconds)
        }
        return false
    }

    override suspend fun login(
        username: String,
        password: String,
    ): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.loginWithCredentials(username, password) {
                if (it.isNotEmpty()) {
                    cont.resume(it.toApiError())
                    return@loginWithCredentials
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun logout(): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.logout {
                if (it.isNotEmpty()) {
                    cont.resume(it.toApiError())
                    return@logout
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun loginWithEmail(email: String): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.loginLink(email) {
                if (it.isNotEmpty()) {
                    cont.resume(it.toApiError())
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
                    cont.resume(it.toApiError())
                    return@loginWithReceipt
                }
                cont.resume(ApiResult.Success)
            }
        }

    override suspend fun migrateToken(apiToken: String): ApiResult =
        suspendCancellableCoroutine { cont ->
            api.migrateApiToken(apiToken) {
                if (it.isNotEmpty()) {
                    cont.resume(it.toApiError())
                    return@migrateApiToken
                }
                cont.resume(ApiResult.Success)
            }
        }

    private fun hasTokens(): Boolean = !api.apiToken().isNullOrEmpty() && !api.vpnToken().isNullOrEmpty()

    private companion object {
        const val LOGIN_CHECK_ATTEMPTS = 5
        const val LOGIN_CHECK_RETRY_DELAY_MS = 300L
    }
}