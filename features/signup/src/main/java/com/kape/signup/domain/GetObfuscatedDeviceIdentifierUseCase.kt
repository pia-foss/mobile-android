package com.kape.signup.domain

import com.kape.signup.data.Identifier
import com.kape.signup.data.Obfuscator

class GetObfuscatedDeviceIdentifierUseCase(
    private val identifier: Identifier,
    private val obfuscator: Obfuscator,
) {

    fun obfuscatedDeviceIdentifier(): Result<String> =
        identifier().mapCatching {
            obfuscator(value = it).getOrThrow()
        }
}