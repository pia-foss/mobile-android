package com.kape.signup.data

import android.util.Base64
import java.security.MessageDigest

internal class ObfuscatorImpl : Obfuscator {

    // region Obfuscator
    override fun invoke(value: String): Result<String> {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(bytes)
        return Result.success(Base64.encodeToString(hashBytes, Base64.NO_WRAP))
    }
    // endregion
}