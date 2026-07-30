package com.kape.ui.utils

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class PriceFormatterTest {
    private val context: Context = mockk(relaxed = true)
    lateinit var priceFormatter: PriceFormatter

    @BeforeEach
    fun setUp() {
        priceFormatter = PriceFormatter(context)
    }

    @ParameterizedTest(name = "priceInMicros: {0}, code: {1}, originalFormattedPrice: {2}, formatted: {3}")
    @MethodSource("arguments")
    fun verifyVariousPrices(
        priceInMicros: Long,
        currencyCode: String,
        originalFormattedPrice: String,
        expected: String,
    ) {
        every { context.getString(any()) } returns PER_MONTH
        val formatted = priceFormatter.formatYearlyPerMonth(priceInMicros, currencyCode, originalFormattedPrice)
        assertEquals(expected, formatted)
    }

    companion object {
        const val PER_MONTH = "%s/mo"

        @JvmStatic
        fun arguments() =
            Stream.of(
                Arguments.of(4_499_000_000L, "GBP", "£4499.00", "£374.92/mo"),
                Arguments.of(4_499_000_000L, "USD", "$4499.00", "$374.92/mo"),
                Arguments.of(4_499_000_000L, "EUR", "4.499,00 €", "€374,92/mo"),
                Arguments.of(12_000_000L, "USD", "$12,00", "$1,00/mo"),
                Arguments.of(1_200_000_000L, "PLN", "1200,00 PLN", "PLN100,00/mo"),
            )
    }
}