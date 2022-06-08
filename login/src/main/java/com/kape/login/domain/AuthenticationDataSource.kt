package com.kape.login.domain

import com.kape.core.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthenticationDataSource {

    fun isUserLoggedIn(): Boolean

    fun login(username: String, password: String): Flow<ApiResult>

    fun logout(): Flow<ApiResult>

    fun loginWithEmail(email: String): Flow<ApiResult>
}