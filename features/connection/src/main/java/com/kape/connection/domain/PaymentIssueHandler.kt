package com.kape.connection.domain

import android.app.Activity

interface PaymentIssueHandler {
    fun checkForPaymentIssues(activity: Activity)
}