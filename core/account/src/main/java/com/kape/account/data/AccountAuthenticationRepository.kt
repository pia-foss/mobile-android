package com.kape.account.data

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.data.auth.ApiError
import com.kape.data.auth.getApiError
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

private const val RECEIPT_STORE = "google_play"

@Singleton(binds = [AuthenticationRepository::class])
internal class AccountAuthenticationRepository(
    private val api: AndroidAccountAPI,
) : AuthenticationRepository {
    // apiToken() and vpnToken() only read an in-memory cache that the account library fills lazily,
    // so after a cold start they are null until the stored tokens are loaded.
    override suspend fun isLoggedIn(): Boolean {
        if (!hasTokens()) loadPersistedTokens()
        return hasTokens()
    }

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
    ): LoginResult = request { api.loginWithCredentials(username, password, it) }

    override suspend fun requestLoginLink(email: String): LoginResult = request { api.loginLink(email, it) }

    override suspend fun loginWithReceipt(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ): LoginResult = request { api.loginWithReceipt(RECEIPT_STORE, purchaseToken, productId, packageName, it) }

    override suspend fun loginWithToken(apiToken: String): LoginResult = request { api.migrateApiToken(apiToken, it) }

    override suspend fun logout(): Boolean = request { api.logout(it) } == LoginResult.Success

    private suspend fun request(call: (callback: (List<AccountRequestError>) -> Unit) -> Unit): LoginResult =
        suspendCancellableCoroutine { continuation ->
            call { errors -> continuation.resume(errors.toLoginResult()) }
        }

    private suspend fun loadPersistedTokens() =
        suspendCancellableCoroutine { continuation ->
            api.loadPersistedTokens { continuation.resume(Unit) }
        }

    private fun hasTokens(): Boolean = !api.apiToken().isNullOrEmpty() && !api.vpnToken().isNullOrEmpty()
}

internal fun List<AccountRequestError>.toLoginResult(): LoginResult {
    val error = lastOrNull() ?: return LoginResult.Success
    return when (getApiError(error.code)) {
        ApiError.AuthFailed -> LoginResult.Failed
        ApiError.AccountExpired -> LoginResult.Expired
        ApiError.Throttled -> LoginResult.Throttled
        ApiError.Unknown -> LoginResult.ServiceUnavailable(error.code, error.message)
    }
}