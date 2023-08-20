package com.kape.signup.data

import com.kape.signup.domain.EmailDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EmailDataSourceImpl(private val api: AndroidAccountAPI) : EmailDataSource, KoinComponent {

    override fun setEmail(email: String): Flow<Boolean> = callbackFlow {
        api.setEmail(email, false) { temporaryPassword, error ->
            if (error.isNotEmpty()) {
                trySend(false)
                return@setEmail
            }
            trySend(true)
        }
        awaitClose { channel.close() }
    }
}