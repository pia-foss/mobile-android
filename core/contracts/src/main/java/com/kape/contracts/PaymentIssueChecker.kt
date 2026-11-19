package com.kape.contracts

import android.app.Activity

fun interface PaymentIssueChecker {
    fun checkForPaymentIssues(activity: Activity)
}