package com.kape.obfuscator.domain

import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.presenter.ObfuscatorAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StartObfuscatorProcess(
    private val obfuscatorAPI: ObfuscatorAPI,
    private val startObfuscatorProcessEventHandler: StartObfuscatorProcessEventHandler,
) {

    companion object {
        private const val LOCALHOST_PROXY = "127.0.0.1"
        private const val OBFUSCATOR_PROXY_PORT = 8383
    }

    operator fun invoke(
        obfuscatorProcessInformation: ObfuscatorProcessInformation,
        obfuscatorProcessListener: ObfuscatorProcessListener,
    ): Flow<Result<Int>> = callbackFlow {
        obfuscatorAPI.start(
            commandLineParams = listOf(
                "-v", "--log-without-time",
                "-s", "${obfuscatorProcessInformation.serverIp}:${obfuscatorProcessInformation.serverPort}",
                "-k", obfuscatorProcessInformation.serverKey,
                "-b", "$LOCALHOST_PROXY:$OBFUSCATOR_PROXY_PORT",
                "-m", obfuscatorProcessInformation.serverEncryptMethod,
            ),
            obfuscatorProcessEventHandler = startObfuscatorProcessEventHandler(
                obfuscatorProcessListener = obfuscatorProcessListener,
            ),
        ) {
            it.fold(
                onSuccess = {
                    trySend(Result.success(OBFUSCATOR_PROXY_PORT))
                },
                onFailure = { throwable ->
                    trySend(Result.failure(throwable))
                },
            )
        }
        awaitClose { channel.close() }
    }
}