package com.kape.payments.domain

import android.app.Activity

interface PaymentIssueHandler {
    fun checkForPaymentIssues(activity: Activity)
}