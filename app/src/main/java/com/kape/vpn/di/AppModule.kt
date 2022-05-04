package com.kape.vpn.di

import android.content.Context
import com.kape.login.BuildConfig
import com.kape.vpn.provider.AccountModuleStateProvider
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import org.koin.dsl.module
import java.io.BufferedReader

val appModule = module {
    single { provideCertificate(get()) }
    single { AccountModuleStateProvider(get()) }
    single { provideAndroidAccountApi(get()) }
}

private fun provideAndroidAccountApi(
    provider: AccountModuleStateProvider
): AndroidAccountAPI {

    return AccountBuilder<AndroidAccountAPI>()
        .setEndpointProvider(provider)
        .setCertificate(provider.certificate)
        .setPlatform(Platform.ANDROID)
        .setUserAgentValue(provideUserAgent())
        .build()
}

private fun provideCertificate(context: Context) = context.assets.open("rsa4096.pem").bufferedReader().use(BufferedReader::readText)
private fun provideUserAgent() = "privateinternetaccess.com Android Client/${BuildConfig.VERSION_CODE}(${BuildConfig.VERSION_CODE})"