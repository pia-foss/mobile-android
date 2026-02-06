package com.kape.ui.utils

import android.content.Context
import java.text.NumberFormat
import java.util.Currency

class PriceFormatter(private val context: Context) {

    fun formatYearlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.yearly_ending).format(cost)
    }

    fun formatYearlyPerMonth(cost: String, currencyCode: String): String {
        val priceInDigits = toEnglishDigits(cost)
        return try {
            val costPerMonth = priceInDigits.toFloat() / 12
            context.getString(com.kape.ui.R.string.yearly_month_ending)
                .format(formatPrice(costPerMonth.toDouble(), currencyCode))
        } catch (e: NumberFormatException) {
            val errorMessage = "${e.message}, $cost"
            throw Exception(errorMessage)
        }
    }

    fun formatMonthlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.monthly_ending).format(cost)
    }

    private fun formatPrice(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
    }

    fun toEnglishDigits(price: String): Double {
        // 1) Convert all localized digits → Latin digits
        val normalizedDigits = price.map { c ->
            val d = Character.getNumericValue(c)
            if (d in 0..9) d else c
        }.joinToString("")

        // 2) Keep only numbers, commas and dots
        val cleaned = normalizedDigits.replace(Regex("[^0-9.,]"), "")

        // 3) Handle decimal separator intelligently
        val lastComma = cleaned.lastIndexOf(',')
        val lastDot = cleaned.lastIndexOf('.')

        val normalizedNumber = when {
            lastComma > lastDot -> cleaned
                .replace(".", "")
                .replace(",", ".")

            else -> cleaned.replace(",", "")
        }

        return normalizedNumber.toDouble()
    }

}