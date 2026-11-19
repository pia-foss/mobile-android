package com.kape.login.domain

import org.koin.core.annotation.Singleton

private const val SUPPORT_THRESHOLD = 2

@Singleton
class LoginFailureTracker {
    private var consecutiveServiceFailures = 0

    fun recordSuccess() {
        consecutiveServiceFailures = 0
    }

    fun recordOtherFailure() {
        consecutiveServiceFailures = 0
    }

    fun recordServiceFailure(): Boolean {
        consecutiveServiceFailures++
        return consecutiveServiceFailures >= SUPPORT_THRESHOLD
    }
}