package com.kape.account.domain

private const val DAY_MS = 24 * 60 * 60 * 1000L

data class AccountDetails(
    val username: String,
    val isExpired: Boolean,
    val daysRemaining: Int,
    val showExpiryWarning: Boolean,
) {
    fun expiresAtMillis(nowMillis: Long): Long = nowMillis + daysRemaining * DAY_MS
}