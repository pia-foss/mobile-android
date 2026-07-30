package com.kape.ui.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import org.koin.core.annotation.Singleton
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency

private const val MICROS_PER_UNIT = 1_000_000.0

@Singleton
class PriceFormatter(
    private val context: Context,
) {
    fun formatYearlyPlan(
        cost: String,
        slashVersion: Boolean = false,
    ): String =
        context
            .getString(
                if (slashVersion) {
                    com.kape.ui.R.string.year_ending
                } else {
                    com.kape.ui.R.string.yearly_ending
                },
            ).format(cost)

    fun formatYearlyPerMonth(
        priceInMicros: Long,
        currencyCode: String,
        originalFormattedPrice: String,
    ): String {
        val costPerMonth = priceInMicros / MICROS_PER_UNIT / 12
        return context
            .getString(com.kape.ui.R.string.yearly_month_ending)
            .format(formatPrice(costPerMonth, currencyCode, originalFormattedPrice))
    }

    fun formatMonthlyPlan(cost: String): String = context.getString(com.kape.ui.R.string.monthly_ending).format(cost)

    @VisibleForTesting
    private fun formatPrice(
        amount: Double,
        currencyCode: String,
        originalFormattedPrice: String,
    ): String {
        val currency = Currency.getInstance(currencyCode)
        val format = NumberFormat.getCurrencyInstance()
        format.currency = currency
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

    // Google Play formats prices using the price's own script and locale conventions - both the
    // digits themselves (Latin, Arabic-Indic, Devanagari, Thai, ...) and the decimal separator
    // can differ from what the device's default locale would otherwise produce. Reusing both
    // (rather than matching specific separator characters) keeps the computed per-month price
    // consistent with the price already on screen, regardless of script.
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