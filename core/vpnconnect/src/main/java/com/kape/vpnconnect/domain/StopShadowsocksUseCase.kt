package com.kape.vpnconnect.domain

import com.kape.obfuscator.domain.StopObfuscatorProcess

internal class StopShadowsocksUseCase(private val stopObfuscatorProcess: StopObfuscatorProcess) {
    operator suspend fun invoke(): Boolean {
        return stopObfuscatorProcess().isSuccess
    }
}