package com.kape.signup.data

import com.kape.signup.domain.EmailDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

@Singleton([EmailDataSource::class])
class EmailDataSourceImpl(private val api: AndroidAccountAPI) : EmailDataSource {

    override suspend fun setEmail(email: String): Boolean = suspendCancellableCoroutine { cont ->
        api.setEmail(email, false) { temporaryPassword, error ->
            if (error.isNotEmpty()) {
                cont.resume(false)
                return@setEmail
            }
            cont.resume(true)
        }
    }
}