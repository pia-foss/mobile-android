package com.kape.signup.domain

import com.kape.signup.data.models.Credentials

interface SignupDataSource {

    /**
     * vararg for Amazon: userId, receiptId
     * vararg for Google: orderId, token, productId
     */

    suspend fun vpnSignup(vararg data: String): Credentials?
}