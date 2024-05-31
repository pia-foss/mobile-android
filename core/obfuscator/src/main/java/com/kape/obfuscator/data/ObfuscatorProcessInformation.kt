package com.kape.obfuscator.data

data class ObfuscatorProcessInformation(
    val serverIp: String,
    val serverPort: String,
    val serverKey: String,
    val serverEncryptMethod: String,
)