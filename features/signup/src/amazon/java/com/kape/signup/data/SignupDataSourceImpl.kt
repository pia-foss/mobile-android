package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AmazonSignupInformation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([SignupDataSource::class])
class SignupDataSourceImpl(
    private val api: AndroidAccountAPI,
) : SignupDataSource {
    override suspend fun vpnSignup(vararg data: String): Credentials? =
        suspendCancellableCoroutine { cont ->
            api.amazonSignUp(AmazonSignupInformation(data[0], data[1], data[2])) { details, error ->
                if (error.isNotEmpty() || details == null) {
                    cont.resume(null)
                    return@amazonSignUp
                }
                cont.resume(Credentials(details.status, details.username, details.password))
            }
        }
}