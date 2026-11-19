package com.kape.login.domain

fun interface StoreDataCleaner {
    suspend fun clear()
}