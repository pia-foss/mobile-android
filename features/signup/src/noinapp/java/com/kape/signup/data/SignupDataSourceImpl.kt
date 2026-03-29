package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

private const val STORE = "noinapp"

@Singleton([SignupDataSource::class])
class SignupDataSourceImpl(private val api: AndroidAccountAPI) : SignupDataSource {

    override fun vpnSignup(vararg data: String): Flow<Credentials?> = flow { emit(null) }
}