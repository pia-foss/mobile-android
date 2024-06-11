package com.kape.ui.utils

import android.content.Context

class PriceFormatter(private val context: Context) {

    fun formatYearlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.yearly_ending).format(cost)
    }

    fun formatYearlyPerMonth(cost: String): String {
        val cleaned = cost.replace("\\D+".toRegex(), "")
        val currency = cost.replace("[0-9.,]".toRegex(), "")
        if (cleaned.isEmpty()) {
            return ""
        }

        return try {
            val costPerMonth = cleaned.toFloat() / 100 / 12
            context.getString(com.kape.ui.R.string.yearly_month_ending).format("%.2f".format(costPerMonth), currency)
        } catch (e: NumberFormatException) {
            val errorMessage = "${e.message}, $cleaned"
            throw Exception(errorMessage)
        }
    }

    fun formatMonthlyPlan(cost: String): String {
        return context.getString(com.kape.ui.R.string.monthly_ending).format(cost)
    }
}