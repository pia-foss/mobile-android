package com.kape.account.domain

interface AccountDetailsRepository {
    suspend fun getAccountDetails(): AccountDetails?
}