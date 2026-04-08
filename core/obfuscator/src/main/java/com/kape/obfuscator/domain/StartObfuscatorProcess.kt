package com.kape.obfuscator.domain

import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.presenter.ObfuscatorAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton
class StartObfuscatorProcess(
    private val obfuscatorAPI: ObfuscatorAPI,
    private val startObfuscatorProcessEventHandler: StartObfuscatorProcessEventHandler,
) {

    companion object {
        const val OBFUSCATOR_PROXY_HOST = "127.0.0.1"
        const val OBFUSCATOR_PROXY_PORT = 8383
    }

    suspend operator fun invoke(
        obfuscatorProcessInformation: ObfuscatorProcessInformation,
        obfuscatorProcessListener: ObfuscatorProcessListener,
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        var isCompleted = false
        obfuscatorAPI.start(
            commandLineParams = listOf(
                "-vvv", "--log-without-time",
                "-s", "${obfuscatorProcessInformation.serverIp}:${obfuscatorProcessInformation.serverPort}",
                "-k", obfuscatorProcessInformation.serverKey,
                "-b", "$OBFUSCATOR_PROXY_HOST:$OBFUSCATOR_PROXY_PORT",
                "-m", obfuscatorProcessInformation.serverEncryptMethod,
            ),
            obfuscatorProcessEventHandler = startObfuscatorProcessEventHandler(
                obfuscatorProcessListener = obfuscatorProcessListener,
            ),
        ) { result ->
            if (!continuation.isActive || isCompleted) return@start
            isCompleted = true
            result.fold(
                onSuccess = {
                    continuation.resume(Result.success(Unit))
                },
                onFailure = { throwable ->
                    continuation.resume(Result.failure(throwable))
                },
            )
        }

        // 🔥 Cancellation hook
        continuation.invokeOnCancellation {
            if (isCompleted) return@invokeOnCancellation
            isCompleted = true
            obfuscatorAPI.stop {
                // Optional: log result, but DO NOT resume continuation here
            }
        }
    }
}