package com.kape.appbar.viewmodel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class FormatCountdownTest {
    @ParameterizedTest
    @CsvSource(
        "1, 0:01",
        "1000, 0:01",
        "1001, 0:02",
        "59000, 0:59",
        "60000, 1:00",
        "3599000, 59:59",
        "3600000, 1:00:00",
        "3661500, 1:01:02",
    )
    fun `rounds up to whole seconds and adds hours when needed`(
        remainingMillis: Long,
        expected: String,
    ) {
        assertEquals(expected, formatCountdown(remainingMillis))
    }
}