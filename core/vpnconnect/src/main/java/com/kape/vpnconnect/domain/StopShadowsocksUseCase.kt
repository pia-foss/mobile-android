package com.kape.vpnconnect.domain

import com.kape.obfuscator.domain.StopObfuscatorProcess

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class StopShadowsocksUseCase(private val stopObfuscatorProcess: StopObfuscatorProcess) {

    operator suspend fun invoke(): Boolean {
        val context = currentCoroutineContext()
        context.ensureActive()
        return stopObfuscatorProcess().isSuccess
    }
}