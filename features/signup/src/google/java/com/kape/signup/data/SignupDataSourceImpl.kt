package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidVpnSignupInformation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

private const val STORE = "google_play"

@Singleton([SignupDataSource::class])
class SignupDataSourceImpl(private val api: AndroidAccountAPI) : SignupDataSource {

    override suspend fun vpnSignup(vararg data: String): Credentials? = suspendCancellableCoroutine { cont ->
        val receipt = AndroidVpnSignupInformation.Receipt(data[0], data[1], data[2])
        api.vpnSignUp(AndroidVpnSignupInformation(store = STORE, receipt = receipt, obfuscatedDeviceId = data[3])) { details, error ->
            if (error.isNotEmpty() || details == null) {
                cont.resume(null)
                return@vpnSignUp
            }
            cont.resume(Credentials(details.status, details.username, details.password))
        }
    }
}