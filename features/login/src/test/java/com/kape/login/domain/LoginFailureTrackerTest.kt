package com.kape.login.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class LoginFailureTrackerTest {
    private val tracker = LoginFailureTracker()

    @Test
    fun `offers support after the second service failure in a row`() {
        assertFalse(tracker.recordServiceFailure())
        assertTrue(tracker.recordServiceFailure())
    }

    @Test
    fun `any other outcome starts the count again`() {
        tracker.recordServiceFailure()
        tracker.recordOtherFailure()
        assertFalse(tracker.recordServiceFailure())

        tracker.recordSuccess()
        assertFalse(tracker.recordServiceFailure())
    }
}