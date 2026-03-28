package com.kape.obfuscator.di

import android.content.Context
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StartObfuscatorProcessEventHandler
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.obfuscator.presenter.ObfuscatorAPI
import com.kape.obfuscator.presenter.ObfuscatorBuilder
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.dsl.module

@Module
class ObfuscatorModule {
    @Singleton([ObfuscatorAPI::class])
    fun provideObfuscatorApi(
        context: Context,
    ): ObfuscatorAPI {
        return ObfuscatorBuilder()
            .setContext(context)
            .setClientCoroutineContext(Dispatchers.Main)
            .build()
    }
}