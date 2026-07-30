package com.kape.login.domain.mobile

import org.koin.core.annotation.Singleton

private const val QUALIFYING_FAILURE_THRESHOLD = 2

@Singleton
class LoginFailureTracker {
    private var consecutiveQualifyingFailures = 0

    fun recordSuccess() {
        consecutiveQualifyingFailures = 0
    }

    fun recordNonQualifyingFailure() {
        consecutiveQualifyingFailures = 0
    }

    fun recordQualifyingFailure(): Boolean {
        consecutiveQualifyingFailures++
        return consecutiveQualifyingFailures >= QUALIFYING_FAILURE_THRESHOLD
    }
}