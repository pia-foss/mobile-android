package com.kape.contracts

import kotlinx.coroutines.flow.Flow

interface DedicatedIpHome {
    fun observeSignupBannerVisible(): Flow<Boolean>

    suspend fun hideSignupBanner()

    suspend fun renewDedicatedIps()
}