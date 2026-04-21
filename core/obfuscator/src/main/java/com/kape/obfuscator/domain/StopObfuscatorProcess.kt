package com.kape.obfuscator.domain

import com.kape.obfuscator.presenter.ObfuscatorAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton
class StopObfuscatorProcess(
    private val obfuscatorAPI: ObfuscatorAPI,
) {
    suspend operator fun invoke(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            obfuscatorAPI.stop {
                continuation.resume(it)
            }
        }
}