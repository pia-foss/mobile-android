package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AmazonSignupInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class SignupDataSourceImpl(private val api: AndroidAccountAPI) : SignupDataSource, KoinComponent {

    override fun vpnSignup(vararg data: String): Flow<Credentials?> = callbackFlow {
        api.amazonSignUp(AmazonSignupInformation(data[0], data[1], data[2])) { details, error ->
            if (error.isNotEmpty() || details == null) {
                trySend(null)
                return@amazonSignUp
            }
            trySend(Credentials(details.status, details.username, details.password))
        }
        awaitClose { channel.close() }
    }
}