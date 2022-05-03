package com.kape.login.domain

import com.kape.login.data.LoginRepository
import com.kape.login.utils.ApiError
import com.kape.login.utils.ApiResult
import com.kape.login.utils.LoginState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(private val repo: LoginRepository) {

    suspend fun login(username: String, password: String): Flow<LoginState> = flow {
        repo.login(username, password).collect {
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