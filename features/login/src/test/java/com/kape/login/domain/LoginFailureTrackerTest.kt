package com.kape.login.domain

import com.kape.login.domain.mobile.LoginFailureTracker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoginFailureTrackerTest {
    private lateinit var tracker: LoginFailureTracker

    @BeforeEach
    internal fun setUp() {
        tracker = LoginFailureTracker()
    }

    @Test
    fun `first qualifying failure does not reach threshold`() {
        assertFalse(tracker.recordQualifyingFailure())
    }

    @Test
    fun `second consecutive qualifying failure reaches threshold`() {
        tracker.recordQualifyingFailure()
        assertTrue(tracker.recordQualifyingFailure())
    }

    @Test
    fun `success resets the counter`() {
        tracker.recordQualifyingFailure()
        tracker.recordSuccess()
        assertFalse(tracker.recordQualifyingFailure())
    }

    @Test
    fun `non-qualifying failure resets the counter`() {
        tracker.recordQualifyingFailure()
        tracker.recordNonQualifyingFailure()
        assertFalse(tracker.recordQualifyingFailure())
    }

    @Test
    fun `threshold keeps being reached on further consecutive failures`() {
        tracker.recordQualifyingFailure()
        assertTrue(tracker.recordQualifyingFailure())
        assertEquals(true, tracker.recordQualifyingFailure())
    }
}