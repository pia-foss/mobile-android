package com.kape.login.domain.mobile

import com.kape.login.utils.LoginState
import com.kape.utils.ApiError
import com.kape.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(private val source: AuthenticationDataSource) {

    suspend fun login(username: String, password: String): Flow<LoginState> = flow {
        source.login(username, password).collect {
            when (it) {
                ApiResult.Success -> emit(LoginState.Successful)
                is ApiResult.Error -> {
                    when (it.error) {
                        ApiError.AccountExpired -> emit(LoginState.Expired)
                        ApiError.AuthFailed -> emit(LoginState.Failed)
                        ApiError.Throttled -> emit(LoginState.Throttled)
                        ApiError.Unknown -> emit(LoginState.Failed)
                    }
                }
            }
        }
    }

    suspend fun loginWithEmail(email: String): Flow<LoginState> = flow {
        source.loginWithEmail(email).collect {
            when (it) {
                ApiResult.Success -> emit(LoginState.Successful)
                is ApiResult.Error -> {
                    when (it.error) {
                        ApiError.AccountExpired -> emit(LoginState.Expired)
                        ApiError.AuthFailed -> emit(LoginState.Failed)
                        ApiError.Throttled -> emit(LoginState.Throttled)
                        ApiError.Unknown -> emit(LoginState.Failed)
                    }
                }
            }
        }
    }

    suspend fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        packageName: String,
    ): Flow<LoginState> = flow {
        source.loginWithReceipt(receiptToken, productId, packageName).collect {
            when (it) {
                ApiResult.Success -> emit(LoginState.Successful)
                is ApiResult.Error -> {
                    when (it.error) {
                        ApiError.AccountExpired -> emit(LoginState.Expired)
                        ApiError.AuthFailed -> emit(LoginState.Failed)
                        ApiError.Throttled -> emit(LoginState.Throttled)
                        ApiError.Unknown -> emit(LoginState.Failed)
                    }
                }
            }
        }
    }
}