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


    @ParameterizedTest(name = "price: {0}, code: {1}, formatted: {2}")
    @MethodSource("arguments")
    fun verifyVariousPrices(price: String, currencyCode: String, expected: String) {
        every { context.getString(any()) } returns PER_MONTH
        val formatted = priceFormatter.formatYearlyPerMonth(price, currencyCode)
        assertEquals(expected, formatted)
    }

    companion object {
        const val PERSIAN = "۴۴۹۹"
        const val PERSIAN_CODE = "IRR"
        const val ARABIC = "٤٤٩٩"
        const val ARABIC_CODE = "SAR"
        const val HINDI = "४,४९९"
        const val ENGLISH = "4499"

        const val PER_MONTH = "%s/mo"

        @JvmStatic
        fun arguments() = Stream.of(
            Arguments.of(PERSIAN, PERSIAN_CODE, "IRR374.92/mo"),
            Arguments.of(ARABIC, ARABIC_CODE, "SAR374.92/mo"),
            Arguments.of(HINDI, "INR", "₹0.37/mo"),
            Arguments.of(ENGLISH, "GBP", "£374.92/mo"),
            Arguments.of(ENGLISH, "EUR", "€374.92/mo"),
        )
    }
}