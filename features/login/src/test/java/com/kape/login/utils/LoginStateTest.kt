package com.kape.login.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoginStateTest {
    @Test
    fun `throttled and service-unavailable are qualifying failures`() {
        assertTrue(isQualifyingFailure(LoginState.Throttled(429, null)))
        assertTrue(isQualifyingFailure(LoginState.ServiceUnavailable(600, null)))
    }

    @Test
    fun `account-state failures are not qualifying failures`() {
        assertFalse(isQualifyingFailure(LoginState.Failed))
        assertFalse(isQualifyingFailure(LoginState.Expired))
    }

    private fun isQualifyingFailure(state: LoginState) = state is QualifyingFailure
}