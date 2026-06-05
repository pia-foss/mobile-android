package com.kape.login.domain

interface LogoutHandler {
    suspend fun clearLocalStorage()
}