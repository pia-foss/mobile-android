package com.kape.profile.models

import java.text.SimpleDateFormat

data class Subscription(
        val isRenewing: Boolean,
        val expirationDate: String
) {
    companion object {
        val DATE_FORMAT = SimpleDateFormat("MM dd, yyyy")
    }
}
