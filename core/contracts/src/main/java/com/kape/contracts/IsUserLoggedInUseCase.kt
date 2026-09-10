package com.kape.contracts

interface IsUserLoggedInUseCase {
    suspend fun invoke(retryOnColdStart: Boolean = false): Boolean
}