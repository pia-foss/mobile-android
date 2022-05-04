package com.kape.login.domain

import com.kape.core.ApiResult
import com.kape.login.data.AuthenticationDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutUseCase(private val source: AuthenticationDataSource) {

    suspend fun logout(): Flow<Boolean> = flow {
        source.logout().collect {
            when (it) {
                ApiResult.Success -> emit(true)
                is ApiResult.Error -> emit(false)
            }
        }
    }
}