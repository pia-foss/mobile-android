package com.kape.signup.domain

import com.kape.signup.data.models.Credentials
import kotlinx.coroutines.flow.Flow

interface SignupDataSource {

    /**
     * vararg for Amazon: userId, receiptId
     * vararg for Google: orderId, token, productId
     */

    fun vpnSignup(vararg data: String): Flow<Credentials?>
}