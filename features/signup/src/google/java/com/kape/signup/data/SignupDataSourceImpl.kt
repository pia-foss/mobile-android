package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidVpnSignupInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

private const val STORE = "google_play"

class SignupDataSourceImpl(private val api: AndroidAccountAPI) : SignupDataSource, KoinComponent {

    override fun vpnSignup(vararg data: String): Flow<Credentials?> = callbackFlow {
        val receipt = AndroidVpnSignupInformation.Receipt(data[0], data[1], data[2])
        api.vpnSignUp(AndroidVpnSignupInformation(store = STORE, receipt = receipt, obfuscatedDeviceId = data[3])) { details, error ->
            if (error.isNotEmpty() || details == null) {
                trySend(null)
                return@vpnSignUp
            }
            trySend(Credentials(details.status, details.username, details.password))
        }
        awaitClose { channel.close() }
    }
}