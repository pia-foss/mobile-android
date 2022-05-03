package com.kape.login.di

import android.content.Context
import com.kape.login.BuildConfig
import com.kape.login.data.LoginRepository
import com.kape.login.domain.LoginUseCase
import com.kape.login.domain.LogoutUseCase
import com.kape.login.provider.AccountModuleStateProvider
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.Prefs
import com.privateinternetaccess.account.AccountBuilder
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.BufferedReader

val loginModule = module {
    single { Prefs(get(), "pia") }
    single { provideCertificate(get()) }
    single { AccountModuleStateProvider(get()) }
    single { provideAndroidAccountApi(get()) }
    single { LoginRepository(get(), get()) }
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    viewModel { LoginViewModel(get()) }
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