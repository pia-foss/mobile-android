package com.kape.obfuscator.di

import android.content.Context
import com.kape.obfuscator.presenter.ObfuscatorAPI
import com.kape.obfuscator.presenter.ObfuscatorBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class ObfuscatorModule {
    @Singleton([ObfuscatorAPI::class])
    fun provideObfuscatorApi(context: Context): ObfuscatorAPI =
        ObfuscatorBuilder()
            .setContext(context)
            .setClientCoroutineContext(Dispatchers.Main)
            .build()
}