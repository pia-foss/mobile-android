package com.kape.settings.data

enum class DataEncryption(val value: String) {
    AES_128_GCM("AES-128-GCM"),
    AES_256_GCM("AES-256-GCM"),
    CHA_CHA_20("ChaCha20"), ;
}