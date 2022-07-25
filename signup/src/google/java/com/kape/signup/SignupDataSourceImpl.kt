package com.kape.signup

import com.kape.signup.domain.SignupDataSource
import com.kape.signup.models.Credentials
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val STORE = "google_play"

class SignupDataSourceImpl : SignupDataSource, KoinComponent {

    private val api: AndroidAccountAPI by inject()

    override fun signup(vararg data: String): Flow<Credentials?> = callbackFlow {
        val receipt = AndroidSignupInformation.Receipt(data[0], data[1], data[2])
        api.signUp(AndroidSignupInformation(STORE, receipt)) { details, error ->
            if (error.isNotEmpty() || details == null) {
                trySend(null)
                return@signUp
            }
            trySend(Credentials(details.status, details.username, details.password))
        }
        awaitClose { channel.close() }
    }
}