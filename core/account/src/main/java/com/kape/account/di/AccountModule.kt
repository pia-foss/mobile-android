package com.kape.account.di

import com.kape.contracts.di.Qualifiers
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.IAccountEndpointProvider
import com.privateinternetaccess.account.Platform
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.account")
class AccountModule {
    @Singleton
    fun provideAccountApi(
        endpointProvider: IAccountEndpointProvider,
        @Named(Qualifiers.CERTIFICATE) certificate: String,
        @Named(Qualifiers.USER_AGENT) userAgent: String,
    ): AndroidAccountAPI =
        AccountBuilder<AndroidAccountAPI>()
            .setEndpointProvider(endpointProvider)
            .setCertificate(certificate)
            .setPlatform(Platform.ANDROID)
            .setUserAgentValue(userAgent)
            .build()
}