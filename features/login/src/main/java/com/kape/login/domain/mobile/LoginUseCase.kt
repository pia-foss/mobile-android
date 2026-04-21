package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import com.kape.data.auth.ApiError
import com.kape.data.auth.ApiResult
import com.kape.login.utils.LoginState
import org.koin.core.annotation.Singleton

@Singleton
class LoginUseCase(
    private val source: AuthenticationDataSource,
) {
    suspend fun login(
        username: String,
        password: String,
    ): LoginState =
        when (val result = source.login(username, password)) {
            ApiResult.Success -> LoginState.Successful
            is ApiResult.Error ->
                when (result.error) {
                    ApiError.AccountExpired -> LoginState.Expired
                    ApiError.AuthFailed -> LoginState.Failed
                    ApiError.Throttled -> LoginState.Throttled
                    ApiError.Unknown -> LoginState.Failed
                }
        }

    suspend fun loginWithEmail(email: String): LoginState =
        when (val result = source.loginWithEmail(email)) {
            ApiResult.Success -> LoginState.Successful
            is ApiResult.Error ->
                when (result.error) {
                    ApiError.AccountExpired -> LoginState.Expired
                    ApiError.AuthFailed -> LoginState.Failed
                    ApiError.Throttled -> LoginState.Throttled
                    ApiError.Unknown -> LoginState.Failed
                }
        }

    suspend fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): LoginState =
        when (val result = source.loginWithReceipt(receiptToken, productId, packageName)) {
            ApiResult.Success -> LoginState.Successful
            is ApiResult.Error ->
                when (result.error) {
                    ApiError.AccountExpired -> LoginState.Expired
                    ApiError.AuthFailed -> LoginState.Failed
                    ApiError.Throttled -> LoginState.Throttled
                    ApiError.Unknown -> LoginState.Failed
                }
        }
}