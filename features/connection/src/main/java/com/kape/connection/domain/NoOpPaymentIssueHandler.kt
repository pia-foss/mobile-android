package com.kape.connection.domain

import android.app.Activity

class NoOpPaymentIssueHandler : PaymentIssueHandler {
    override fun checkForPaymentIssues(activity: Activity) {
        // no-op
    }
}