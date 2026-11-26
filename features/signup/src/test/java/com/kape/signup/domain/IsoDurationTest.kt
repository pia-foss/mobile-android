package com.kape.signup.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class IsoDurationTest {
    @ParameterizedTest
    @CsvSource("P3D, 3", "P1W, 7", "P2W, 14", "P1M, 30", "P1Y, 365")
    fun `converts single-unit durations to days`(
        duration: String,
        days: Int,
    ) {
        assertEquals(days, isoDurationToDays(duration))
    }

    @Test
    fun `no duration has no days`() {
        assertNull(isoDurationToDays(null))
    }

    @Test
    fun `rejects other formats`() {
        assertThrows<IllegalArgumentException> { isoDurationToDays("P1M2D") }
    }
}