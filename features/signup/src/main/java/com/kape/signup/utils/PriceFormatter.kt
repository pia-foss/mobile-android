package com.kape.signup.utils

import android.content.Context
import com.kape.ui.R
import java.util.Currency
import java.util.Locale

class PriceFormatter(private val context: Context) {

    fun formatYearlyPlan(cost: String): String {
        return context.getString(R.string.yearly_ending).format(cost)
    }

    fun formatYearlyPerMonth(cost: String): String {
        val sb = StringBuilder()
        sb.append("#")
        val c = Currency.getInstance(Locale.getDefault())
        val fractionNumber = c.defaultFractionDigits
        if (fractionNumber > 0) {
            sb.append(".")
            for (i in 0 until fractionNumber) {
                sb.append("#")
            }
        }
        val cleaned = cost.replace("\\D+".toRegex(), "")
        val currency = cost.replace("[0-9.,]".toRegex(), "")
        if (cleaned.isEmpty()) {
            return ""
        }
        val costPerMonth = cleaned.toFloat() / 100 / 12
        return context.getString(R.string.yearly_month_ending).format("%.2f".format(costPerMonth), currency)
    }

    fun formatMonthlyPlan(cost: String): String {
        return context.getString(R.string.monthly_ending).format(cost)
    }
}