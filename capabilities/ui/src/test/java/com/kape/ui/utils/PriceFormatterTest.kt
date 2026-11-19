package com.kape.ui.utils

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class PriceFormatterTest {
    private val defaultLocale = Locale.getDefault()
    private val context = mockk<Context> { every { getString(any()) } returns "%s/month" }
    private val formatter = PriceFormatter(context)

    @BeforeEach
    fun setUp() = Locale.setDefault(Locale.US)

    @AfterEach
    fun tearDown() = Locale.setDefault(defaultLocale)

    @Test
    fun `divides the yearly price by twelve`() {
        assertEquals("$5.00/month", formatter.formatYearlyPerMonth(59_990_000, "USD", "$59.99"))
    }

    @Test
    fun `keeps the store's decimal separator`() {
        assertEquals("€5,00/month", formatter.formatYearlyPerMonth(59_990_000, "EUR", "59,99 €"))
    }

    @Test
    fun `keeps the store's digits`() {
        assertEquals("$٥٫٠٠/month", formatter.formatYearlyPerMonth(59_990_000, "USD", "٥٩٫٩٩ US$"))
    }

    @Test
    fun `rounds currencies without fractions to whole units`() {
        assertEquals("¥417/month", formatter.formatYearlyPerMonth(5_000_000_000, "JPY", "¥5,000"))
    }
}