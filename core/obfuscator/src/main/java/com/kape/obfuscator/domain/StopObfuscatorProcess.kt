package com.kape.obfuscator.domain

import com.kape.obfuscator.presenter.ObfuscatorAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StopObfuscatorProcess(
    private val obfuscatorAPI: ObfuscatorAPI,
) {

    operator fun invoke(): Flow<Result<Unit>> = callbackFlow {
        obfuscatorAPI.stop {
            trySend(it)
        }
        awaitClose { channel.close() }
    }
}