package com.kape.profile.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Duration

data class Subscription(
    val isExpired: Boolean,
    val daysRemaining: Int,
    val showExpire: Boolean,
) {
    val expirationDate: String = getExpirationDate(daysRemaining)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpirationDate(daysRemaining: Int): String {
        val timestamp = System.currentTimeMillis() + Duration.ofDays(daysRemaining.toLong()).toMillis()
        return DATE_FORMAT.format(timestamp)
    }

    companion object {
        val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy")
    }
}