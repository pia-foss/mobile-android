package com.kape.ui.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import java.text.NumberFormat
import java.text.ParseException
import java.util.Currency
import java.util.Locale

class PriceFormatter(private val context: Context) {

    fun formatYearlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.yearly_ending).format(cost)
    }

    fun formatYearlyPerMonth(cost: String, currencyCode: String): String {
        val priceAsDouble = toEnglishDigits(cost)
        priceAsDouble?.let {
            return try {
                val costPerMonth = priceAsDouble / 12
                context.getString(com.kape.ui.R.string.yearly_month_ending)
                    .format(formatPrice(costPerMonth, currencyCode))
            } catch (e: NumberFormatException) {
                ""
            }
        }?: return ""
    }

    fun formatMonthlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.monthly_ending).format(cost)
    }

    @VisibleForTesting
    private fun formatPrice(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
    }

    fun toEnglishDigits(price: String, locale: Locale = Locale.getDefault()): Double? {
        val cleaned = price.replace("[^\\d.,]".toRegex(), "").trim()
        return try {
            NumberFormat.getNumberInstance(locale).parse(cleaned)?.toDouble()
        } catch (e: ParseException) {
            null
        }
    }

}