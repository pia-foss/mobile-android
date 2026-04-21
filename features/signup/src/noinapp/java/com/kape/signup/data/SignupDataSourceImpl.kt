package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.Singleton

private const val STORE = "noinapp"

@Singleton([SignupDataSource::class])
class SignupDataSourceImpl(
    private val api: AndroidAccountAPI,
) : SignupDataSource {
    override suspend fun vpnSignup(vararg data: String): Credentials? = null
}