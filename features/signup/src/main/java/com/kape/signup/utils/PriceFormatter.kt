package com.kape.signup.utils

import android.content.Context
import com.kape.ui.R

class PriceFormatter(private val context: Context) {

    fun formatYearlyPlan(cost: String): String {
        return context.getString(R.string.yearly_ending).format(cost)
    }

    fun formatYearlyPerMonth(cost: String): String {
        var cleaned = cost.replace("\\D+".toRegex(), "")
        val currency = cost.replace("[0-9.,]".toRegex(), "")
        if (cleaned.isEmpty()) {
            return ""
        }

        return try {
            val costPerMonth = cleaned.toFloat() / 100 / 12
            context.getString(R.string.yearly_month_ending).format("%.2f".format(costPerMonth), currency)
        } catch (e: NumberFormatException) {
            ""
        }
    }

    fun formatMonthlyPlan(cost: String): String {
        return context.getString(R.string.monthly_ending).format(cost)
    }
}