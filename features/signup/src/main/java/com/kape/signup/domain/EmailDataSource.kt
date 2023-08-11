package com.kape.signup.domain

import kotlinx.coroutines.flow.Flow

interface EmailDataSource {

    fun setEmail(email: String): Flow<Boolean>
}