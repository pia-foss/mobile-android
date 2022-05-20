package com.kape.profile.models

data class Subscription(
        val isRenewing: Boolean,
        val expirationDate: String
) {
    companion object {
        const val DATE_FORMAT = "MM dd, yyyy"
    }
}
