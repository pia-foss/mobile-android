package com.kape.profile.models

import java.text.SimpleDateFormat
import java.time.Duration

data class Subscription(
    val isExpired: Boolean,
    val daysRemaining: Int,
    val showExpire: Boolean
) {
    val expirationDate: String = getExpirationDate(daysRemaining)

    private fun getExpirationDate(daysRemaining: Int): String {
        val timestamp = System.currentTimeMillis() + Duration.ofDays(daysRemaining.toLong()).toMillis()
        return DATE_FORMAT.format(timestamp)
    }

    companion object {
        val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy")
    }
}
