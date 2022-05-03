package com.kape.login.domain

import com.kape.login.data.LoginRepository
import com.kape.login.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutUseCase(private val repo: LoginRepository) {

    suspend fun logout(): Flow<Boolean> = flow {
        repo.logout().collect {
            when (it) {
                ApiResult.Success -> emit(true)
                is ApiResult.Error -> emit(false)
            }
        }
    }
}