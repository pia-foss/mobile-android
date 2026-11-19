package com.kape.snooze

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class SnoozeCountdownTest {
    @ParameterizedTest
    @CsvSource("900000, 15", "899999, 15", "60001, 2", "60000, 1", "1, 1", "0, 0", "-5000, 0")
    fun `rounds the time left up to whole minutes`(
        remainingMillis: Long,
        minutes: Int,
    ) {
        assertEquals(minutes, minutesUntil(end = 1_000_000 + remainingMillis, now = 1_000_000))
    }
}