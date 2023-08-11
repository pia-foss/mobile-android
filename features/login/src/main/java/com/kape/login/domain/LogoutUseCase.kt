package com.kape.login.domain

import com.kape.utils.ApiResult
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