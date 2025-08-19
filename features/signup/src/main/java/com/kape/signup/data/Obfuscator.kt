package com.kape.signup.data

interface Obfuscator {
    operator fun invoke(value: String): Result<String>
}