package com.kape.ui.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency

private const val MICROS_PER_UNIT = 1_000_000.0

class PriceFormatter(
    private val context: Context,
) {
    fun formatYearlyPerMonth(
        priceInMicros: Long,
        currencyCode: String,
        originalFormattedPrice: String,
    ): String {
        val costPerMonth = priceInMicros / MICROS_PER_UNIT / 12
        return context
            .getString(com.kape.ui.R.string.monthly_ending)
            .format(formatPrice(costPerMonth, currencyCode, originalFormattedPrice))
    }

    private fun formatPrice(
        amount: Double,
        currencyCode: String,
        originalFormattedPrice: String,
    ): String {
        val currency = Currency.getInstance(currencyCode)
        val format = NumberFormat.getCurrencyInstance()
        format.currency = currency
        // The JVM's setCurrency keeps the locale currency's fraction digits (e.g. ¥416.67)
        format.minimumFractionDigits = currency.defaultFractionDigits
        format.maximumFractionDigits = currency.defaultFractionDigits
        val style = digitStyle(originalFormattedPrice, currency.defaultFractionDigits)
        if (format is DecimalFormat && style != null) {
            format.decimalFormatSymbols =
                format.decimalFormatSymbols.apply {
                    zeroDigit = style.zeroDigit
                    decimalSeparator = style.separator
                    monetaryDecimalSeparator = style.separator
                }
        }
        return format.format(amount)
    }

    private fun digitStyle(
        formattedPrice: String,
        fractionDigits: Int,
    ): DigitStyle? {
        if (fractionDigits <= 0) return null

        val digitIndices = formattedPrice.indices.filter { formattedPrice[it].isDigit() }
        if (digitIndices.size < fractionDigits) return null

        val fractionStartIndex = digitIndices[digitIndices.size - fractionDigits]
        val separatorIndex = fractionStartIndex - 1
        if (separatorIndex < 0) return null

        val separator = formattedPrice[separatorIndex]
        if (separator.isDigit()) return null

        val sampleDigit = formattedPrice[digitIndices.first()]
        val zeroDigit = sampleDigit - Character.digit(sampleDigit, 10)
        return DigitStyle(zeroDigit, separator)
    }

    private data class DigitStyle(
        val zeroDigit: Char,
        val separator: Char,
    )
}