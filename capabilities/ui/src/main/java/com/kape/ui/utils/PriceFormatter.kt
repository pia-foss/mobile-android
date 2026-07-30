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
        val separator = decimalSeparator(originalFormattedPrice, currency.defaultFractionDigits)
        if (format is DecimalFormat && separator != null) {
            format.decimalFormatSymbols =
                format.decimalFormatSymbols.apply {
                    decimalSeparator = separator
                    monetaryDecimalSeparator = separator
                }
        }
        return format.format(amount)
    }

    // Google Play formats prices using the price's own locale, which can differ from the device's
    // default locale - reuse its decimal separator so both prices on screen render consistently.
    private fun decimalSeparator(
        formattedPrice: String,
        fractionDigits: Int,
    ): Char? {
        if (fractionDigits <= 0) return null
        return Regex("[.,](?=\\d{$fractionDigits}(?!\\d))")
            .findAll(formattedPrice)
            .lastOrNull()
            ?.value
            ?.first()
    }
}