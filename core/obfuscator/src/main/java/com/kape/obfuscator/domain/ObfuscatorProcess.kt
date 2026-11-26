package com.kape.obfuscator.domain

import com.kape.obfuscator.presenter.ObfuscatorAPI
import com.kape.obfuscator.presenter.ObfuscatorProcessEventHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import kotlin.coroutines.resume

data class ObfuscatorProcessInformation(
    val serverIp: String,
    val serverPort: String,
    val serverKey: String,
    val serverEncryptMethod: String,
)

@Factory
class StartObfuscatorProcess(
    private val api: ObfuscatorAPI,
) {
    suspend operator fun invoke(
        server: ObfuscatorProcessInformation,
        onStopped: () -> Unit,
    ): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            api.start(
                commandLineParams =
                    listOf(
                        "-vvv",
                        "--log-without-time",
                        "-s",
                        "${server.serverIp}:${server.serverPort}",
                        "-k",
                        server.serverKey,
                        "-b",
                        "$PROXY_HOST:$PROXY_PORT",
                        "-m",
                        server.serverEncryptMethod,
                    ),
                obfuscatorProcessEventHandler =
                    object : ObfuscatorProcessEventHandler {
                        override fun processStopped(): Result<Unit> {
                            onStopped()
                            return Result.success(Unit)
                        }
                    },
            ) { result -> continuation.resume(result) }
        }

    companion object {
        const val PROXY_HOST = "127.0.0.1"
        const val PROXY_PORT = 8383
    }
}

@Factory
class StopObfuscatorProcess(
    private val api: ObfuscatorAPI,
) {
    suspend operator fun invoke(): Result<Unit> = suspendCancellableCoroutine { continuation -> api.stop { continuation.resume(it) } }
}